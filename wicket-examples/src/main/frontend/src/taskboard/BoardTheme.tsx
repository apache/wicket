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

import { MantineProvider } from '@mantine/core';
import type { ReactNode } from 'react';
import { cspNonce } from 'wicket-islands-react';
import '@mantine/core/styles/default-css-variables.css';
import '@mantine/core/styles/global.css';
import '@mantine/core/styles/Alert.css';
import '@mantine/core/styles/Avatar.css';
import '@mantine/core/styles/Badge.css';
import '@mantine/core/styles/Button.css';
import '@mantine/core/styles/Card.css';
import '@mantine/core/styles/CloseButton.css';
import '@mantine/core/styles/Combobox.css';
import '@mantine/core/styles/Group.css';
import '@mantine/core/styles/Input.css';
import '@mantine/core/styles/Loader.css';
import '@mantine/core/styles/Paper.css';
import '@mantine/core/styles/Popover.css';
import '@mantine/core/styles/ScrollArea.css';
import '@mantine/core/styles/Stack.css';
import '@mantine/core/styles/Text.css';
import '@mantine/core/styles/Title.css';
import '@mantine/core/styles/UnstyledButton.css';
import './BoardTheme.css';

/**
 * Wraps a task board island in Mantine's provider. Each island is a React root of its own, so each
 * gets one. Mantine's CSS reset is left out, as it would restyle the Wicket page around the
 * islands; the style elements Mantine injects carry the page's CSP nonce.
 */
export default function BoardTheme({ children }: { children: ReactNode }) {
	return (
		<MantineProvider forceColorScheme="light" getStyleNonce={() => cspNonce() ?? ''}>
			<div className="board-theme">{children}</div>
		</MantineProvider>
	);
}
