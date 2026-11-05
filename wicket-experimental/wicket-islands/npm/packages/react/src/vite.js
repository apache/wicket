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

/**
 * Installs React Fast Refresh in a page Vite does not serve itself.
 */
export const reactRefreshPreamble = `import RefreshRuntime from '/@react-refresh';
RefreshRuntime.injectIntoGlobalHook(window);
window.$RefreshReg$ = () => {};
window.$RefreshSig$ = () => (type) => type;
window.__vite_plugin_react_preamble_installed__ = true;
`;

/**
 * The options of the Wicket Islands Vite plugin for React islands: island modules export a plain
 * component, which keeps them eligible for Fast Refresh.
 *
 * ```js
 * wicketIslands({ islands: { 'treasure-board': 'src/TreasureBoard.tsx' }, ...reactIslands })
 * ```
 */
export const reactIslands = {
	adapter: 'wicket-islands-react',
	devPreamble: reactRefreshPreamble
};
