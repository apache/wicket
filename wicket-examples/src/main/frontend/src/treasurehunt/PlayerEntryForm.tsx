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

import { useFormState, useFormValue } from 'wicket-islands-react';
import './PlayerEntryForm.css';

/**
 * The value of the form island, as PlayerEntry.java declares it.
 */
interface PlayerEntry {
	name: string;
	colour: string;
}

/**
 * The props of the form island, as PlayerEntryField.java's EntryProps sends them.
 */
interface EntryProps {
	palette: string[];
}

export default function PlayerEntryForm({ palette }: EntryProps) {
	const [entry, setEntry] = useFormValue<PlayerEntry>();
	const { enabled, errors } = useFormState();
	const name = entry?.name ?? '';
	const colour = entry?.colour ?? palette[0];

	return (
		<div className="player-entry">
			<label>
				Name <input type="text" value={name} maxLength={30} disabled={!enabled}
					onChange={event => setEntry({ name: event.target.value, colour })} />
			</label>
			<div className="swatches" role="radiogroup" aria-label="Colour">
				{palette.map(choice => (
					<button key={choice} type="button" role="radio" aria-checked={choice === colour}
						aria-label={choice} disabled={!enabled}
						className={choice === colour ? 'swatch selected' : 'swatch'}
						style={{ backgroundColor: choice }}
						onClick={() => setEntry({ name, colour: choice })} />
				))}
			</div>
			{errors.length > 0 && (
				<ul className="errors">
					{errors.map(error => <li key={error}>{error}</li>)}
				</ul>
			)}
		</div>
	);
}
