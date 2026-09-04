# wicket-benchmarks

Development aid. Never released, contains no unit tests. It lives in the reactor so the
benchmarks keep compiling against the current API — the previous set of component benchmarks was
only ever attached to [WICKET-6774] and had rotted by the time anyone wanted to re-run them.

## Running

Benchmarks are in `src/main/java`, so a plain `compile` is enough:

```bash
# the JS resource optimizer re-minifies its own output, so wicket-core needs a clean first
mvn -o clean -pl wicket-core
mvn -o -pl wicket-benchmarks -am compile

# classpath for the freshly built classes (not the jars in ~/.m2)
mvn -o -pl wicket-benchmarks dependency:build-classpath \
  -Dmdep.outputFile=wicket-benchmarks/target/ext-cp.txt
CP="wicket-benchmarks/target/classes:wicket-core/target/classes:wicket-util/target/classes:\
wicket-request/target/classes:wicket-tester/target/classes:\
$(cat wicket-benchmarks/target/ext-cp.txt)"
```

Beware: `dependency:build-classpath` lists the **`~/.m2` jars** for `wicket-core` and friends.
If you leave them on the classpath you are measuring whatever was last installed, not your working
copy. Put the `target/classes` directories first, as above, and confirm which implementation you
actually loaded before believing any number.

### Time and allocation

```bash
java -cp "$CP" org.openjdk.jmh.Main ComponentStateBenchmark -prof gc
```

`-prof gc` is not optional in practice: `gc.alloc.rate.norm` (bytes per operation) is the number
that matters for a framework that keeps many pages in memory, and it is far steadier than
throughput.

### Footprint and serialized size

```bash
java --add-opens java.base/java.lang=ALL-UNNAMED -cp "$CP" \
  org.apache.wicket.benchmarks.ComponentFootprint
```

JOL needs the `--add-opens` to walk the graph. Run it a second time with
`-XX:+UseCompactObjectHeaders`: that flag moves every object by 4 bytes and can change which
layout wins, so a footprint claim without it is only half the story.

## What is here, and what each part is for

| | measures | use it for |
|---|---|---|
| `ComponentStateBenchmark` | ns/op and bytes/op of the per-request state accessors | attributing a change to state handling |
| `ComponentFootprint` | retained heap and serialized bytes per state shape | anything about memory |
| `PageRenderBenchmark` | µs/op of a full 50-child panel render | catching an end-to-end regression |

## Reading the results

**Mixed shapes are the interesting ones.** `Component.data` holds a different kind of object
depending on which of model, behaviors and meta data are present. Feed one shape at a time and the
call sites that unpack it are monomorphic and inline, which flatters any implementation that
dispatches on shape. Real pages interleave shapes. A large gap between `readMetaData` and
`readMetaDataMixedShapes` is the signature of dispatch that stopped inlining, and it is invisible
to a per-shape benchmark.

**Do not measure a mutation repeatedly against one instance.** `detach()` is not idempotent: the
first call detaches models, drops temporary behaviors and compacts the behavior array, so every
later call exercises the already-detached path. The original benchmark did exactly this and so
measured the cheap case with great precision. `buildAndDetach` folds construction into the
operation instead, which keeps every invocation doing real work without paying
`Level.Invocation` overhead.

**Measure the shape whose cost you are actually arguing about.** The shapes are not equally
interesting, and the difference is not proportional to how exotic they look. `STABLE_ID_BEHAVIOR`
is the case every link and ajax-enabled component hits, and it is where the storage layout makes by
far the largest difference — master keeps a `BehaviorIdList` plus two `Object[]` per component to
record one behavior id, which is ~72 bytes of heap and ~32 bytes of serialized form per component.
A benchmark built only from `AttributeModifier` never creates that structure and will report a few
percent where the real figure is tens of percent. WICKET-6774's own comments named this as the
main win; the first version of this file missed it entirely.

**Weight results by what real pages contain.** From a production app measured on WICKET-6774
(548,285 components across 2,635 pages): 39% carry a model, 35% at least one behavior, 8% any meta
data, and 0.03% more than one meta data entry. An 80% win on a shape that is 1% of components is
worth less than a 5% win on models.

**Comparing two implementations** means running the same benchmark source against both, because
these deliberately use public API only. Build each tree separately and keep the classpaths
straight; a git worktree per side is the least error-prone way.

[WICKET-6774]: https://issues.apache.org/jira/browse/WICKET-6774
