/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Writes a minified foo.min.js / foo.min.css, with a linked source map, next to every
 * foo.js / foo.css below the given directories. Wicket serves the .min variant when
 * ResourceSettings#getUseMinifiedResources() is on.
 *
 * Usage: node minify.mjs <directory>...
 */

import { readdir, readFile } from 'node:fs/promises';
import { basename, dirname, extname, join } from 'node:path';

let esbuild;
try {
	esbuild = await import('esbuild');
} catch (e) {
	console.error('esbuild is not installed in ' + import.meta.dirname + '. Build the root ' +
		'project once (mvn -N generate-resources) to install it, then rerun this module.');
	process.exit(1);
}

const JS_TARGET = 'es2020';

const ASF_BANNER = '/*! Licensed to the Apache Software Foundation (ASF) under the Apache ' +
	'License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0 */';

const LEADING_COMMENT = /^\s*(\/\*[\s\S]*?\*\/)/;

function isSource(name) {
	const ext = extname(name);
	return (ext === '.js' || ext === '.css')
		&& !name.endsWith('.min' + ext)
		&& !/^jquery.*\.js$/.test(name);
}

async function findSources(dir) {
	const entries = await readdir(dir, { recursive: true, withFileTypes: true });
	return entries
		.filter(entry => entry.isFile() && isSource(entry.name))
		.map(entry => join(entry.parentPath, entry.name));
}

// Third-party files keep their own notice; Wicket's own get a one-line form of the ASF header.
async function banner(file) {
	const source = await readFile(file, 'utf8');
	const match = LEADING_COMMENT.exec(source);
	if (match === null) {
		return '';
	}
	if (match[1].includes('Licensed to the Apache Software Foundation')) {
		return ASF_BANNER;
	}
	return match[1].replace(/^\/\*(?!!)/, '/*!');
}

async function minify(file) {
	const ext = extname(file);
	const loader = ext.substring(1);
	await esbuild.build({
		entryPoints: [file],
		outfile: join(dirname(file), basename(file, ext) + '.min' + ext),
		loader: { [ext]: loader },
		minify: true,
		target: loader === 'js' ? JS_TARGET : undefined,
		sourcemap: 'linked',
		sourcesContent: true,
		legalComments: 'inline',
		banner: { [loader]: await banner(file) },
		logLevel: 'warning'
	});
}

const dirs = process.argv.slice(2);
if (dirs.length === 0) {
	console.error('Usage: node minify.mjs <directory>...');
	process.exit(2);
}

const files = (await Promise.all(dirs.map(findSources))).flat();
await Promise.all(files.map(minify));
console.log('Minified ' + files.length + ' file(s) in ' + dirs.join(', '));
