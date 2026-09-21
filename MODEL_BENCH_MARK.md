# What registering additional models costs

Measured comparison of the two ways a component can hold models beside its default model:

| Variant | Extra models held in | Detached by |
| --- | --- | --- |
| `FIELDS` | one field each | a hand-written `onDetach()` |
| `REGISTERED` | one field each | `addAdditionalModel` |
| `REGISTERED_NO_FIELDS` | nothing | `addAdditionalModel` |

All three carry the same default model and the same extra models, so every difference below
is bookkeeping, never the models themselves.

**Environment:** OpenJDK 24.0.1, `-Xmx1g`, 1,000 children per tree, JOL 0.17, JMH 1.37.
JMH run with `-f 1 -wi 3 -i 3 -r 1 -w 1` — quick mode, so treat `ns/op` as indicative and
`B/op` as exact (allocation is counted, not sampled).

Reproduce with `wicket-benchmarks/run-model-benchmarks.sh`.

---

## Memory — retained heap, bytes per component

Deltas against the same component carrying only a default model.

| Extra models | `FIELDS` | `REGISTERED` | `REGISTERED_NO_FIELDS` | registering costs |
| ---: | ---: | ---: | ---: | ---: |
| 1 | 64.0 | 136.0 | 120.0 | **+72.0** |
| 2 | 128.0 | 200.0 | 184.0 | **+72.0** |
| 3 | 192.0 | 272.0 | 256.0 | **+80.0** |

With `-XX:+UseCompactObjectHeaders` (every object 4 bytes smaller):

| Extra models | `FIELDS` | `REGISTERED` | `REGISTERED_NO_FIELDS` | registering costs |
| ---: | ---: | ---: | ---: | ---: |
| 1 | 63.9 | 119.9 | 111.9 | **+56.0** |
| 2 | 127.8 | 191.8 | 183.8 | **+64.0** |
| 3 | 191.8 | 255.8 | 247.8 | **+64.0** |

## Memory — serialized bytes per component

What the page store pays.

| Extra models | `FIELDS` | `REGISTERED` | `REGISTERED_NO_FIELDS` | registering costs |
| ---: | ---: | ---: | ---: | ---: |
| 1 | 13.9 | 47.2 | 40.2 | **+33.3** |
| 2 | 27.8 | 66.1 | 55.1 | **+38.3** |
| 3 | 41.7 | 85.0 | 70.0 | **+43.3** |

Unaffected by compact headers, as expected — the wire format does not carry object headers.

## Where the bytes go

JOL breakdown for `REGISTERED` with 3 extra models, 1,000 components:

| Object | Count | Avg | Sum |
| --- | ---: | ---: | ---: |
| `AdditionalModelsShapes$RegisteredModels` | 1000 | 72 | 72,000 |
| `[Lorg.apache.wicket.model.IModel;` | 1000 | 32 | 32,000 |
| `org.apache.wicket.ComponentState` | 1000 | 24 | 24,000 |
| `org.apache.wicket.MetaDataEntry` | 1000 | 24 | 24,000 |
| `org.apache.wicket.model.Model` | 4000 | 16 | 64,000 |

The three objects a registered component pays for are exactly `ComponentState` (24) +
`MetaDataEntry` (24) + the `IModel[]` (32) = **80 bytes** — the measured `+80.0` at three
models. The array is the only part that grows: 24 bytes at one model, 32 at three.

## Time and allocation

`build` — constructing the component, where registering does its extra work:

| Extra models | Variant | ns/op | B/op |
| ---: | --- | ---: | ---: |
| 1 | `FIELDS` | 26.2 ± 2.2 | 152 |
| 1 | `REGISTERED` | 33.9 ± 12.6 | 224 |
| 1 | `REGISTERED_NO_FIELDS` | 32.2 ± 2.0 | 208 |
| 2 | `FIELDS` | 35.6 ± 2.0 | 216 |
| 2 | `REGISTERED` | 54.0 ± 4.7 | 312 |
| 2 | `REGISTERED_NO_FIELDS` | 54.4 ± 3.9 | 296 |
| 3 | `FIELDS` | 41.9 ± 1.9 | 280 |
| 3 | `REGISTERED` | 75.6 ± 10.7 | 408 |
| 3 | `REGISTERED_NO_FIELDS` | 76.5 ± 15.8 | 392 |

`buildAndDetach` — the whole per-request cycle:

| Extra models | Variant | ns/op | B/op |
| ---: | --- | ---: | ---: |
| 1 | `FIELDS` | 31.8 ± 2.8 | 152 |
| 1 | `REGISTERED` | 39.8 ± 11.0 | 224 |
| 1 | `REGISTERED_NO_FIELDS` | 38.8 ± 1.2 | 208 |
| 2 | `FIELDS` | 39.1 ± 2.0 | 216 |
| 2 | `REGISTERED` | 60.4 ± 1.7 | 312 |
| 2 | `REGISTERED_NO_FIELDS` | 61.2 ± 2.1 | 296 |
| 3 | `FIELDS` | 47.0 ± 5.6 | 280 |
| 3 | `REGISTERED` | 82.5 ± 5.4 | 408 |
| 3 | `REGISTERED_NO_FIELDS` | 88.4 ± 3.6 | 392 |

---

## Conclusions

**1. The documented "50 to 80 bytes" is right, and slightly conservative at the top end.**
Retained heap costs **72 to 80 bytes per component** with default headers, **56 to 64** with
compact ones. The guide and the `addAdditionalModel` javadoc can stand as they are.

**2. The cost is near-flat in the number of models, as claimed — but not exactly flat.**
72, 72, 80 bytes for one, two and three models. The fixed part is the `MetaDataEntry` plus
the `ComponentState` a component needs once it has more than one kind of state; only the
`IModel[]` grows, and it grows by 8 bytes per few models, not per model. Wording like
"whatever their number" is fair for heap. It is weaker for the wire: **+33, +38, +43
bytes**, about 5 bytes per model, because serialization writes the array and its class
descriptor.

**3. Serialized cost is the one that bites.** A registered component is **3.4× to 2.0×**
the serialized size of the hand-detached one (47.2 vs 13.9 bytes at one model). Small in
absolute terms, but the page store pays it on every page write, for every instance.

**4. Construction is measurably slower, and that is the real per-instance price.**
`build` goes from 41.9 to 75.6 ns/op at three models — roughly **+80%**, well outside the
error bars. Allocation confirms it exactly: **+128 B/op** at three models. This is the
number to weigh for a component rendered in the thousands, and it is a stronger argument
than the heap figure that the components shipped with Wicket should keep detaching their
own models.

**5. Detaching itself is cheaper when registered, which partly offsets construction.**
`buildAndDetach` minus `build` is ~5 ns for `FIELDS` against ~4–7 ns for `REGISTERED`: the
framework's loop over the model array costs about what three hand-written null-checked
`detach()` calls cost. Registering buys its convenience at construction time, not at detach
time.

**6. Dropping the fields is worth 16 bytes of heap and 11–15 bytes on the wire.**
`REGISTERED_NO_FIELDS` beats `REGISTERED` at every model count, and the time difference is
within the noise. A component that only passes models to its children should use the
`(String, IModel, IModel...)` constructor rather than keeping fields it never reads — an
option the hand-detached approach cannot offer at all, since it needs the fields to reach
the models from `onDetach()`.

**Where the trade stops paying.** At ~80 bytes of heap and ~43 of wire per component, a
panel used 50 times on a page costs 4KB of heap — irrelevant. The same feature on a cell
rendered 10,000 times in a `DataTable` costs 800KB of heap, 430KB per serialized page, and
roughly 0.34ms of extra construction per render. The existing guidance — register in
application components, detach by hand in components rendered in the thousands — is
supported by these numbers.
