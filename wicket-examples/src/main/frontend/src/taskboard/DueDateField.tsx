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

import { DatePickerInput } from '@mantine/dates';
import { useFormState, useFormValue } from 'wicket-islands-react';
import BoardTheme from './BoardTheme';
import '@mantine/dates/styles.css';
import './FormFields.css';

/**
 * The due date picker of DueDateField.java. Its value is an ISO date, as Jackson writes a
 * LocalDate. The picker opens inside the island, so it stays within the Wicket dialog.
 */
export default function DueDateField() {
	const [value, setValue] = useFormValue<string>();
	const { enabled, errors, label, required } = useFormState();

	return (
		<BoardTheme>
			<DatePickerInput className="due-date" label={label} value={value ?? null}
				onChange={date => setValue(date ?? undefined)} valueFormat="YYYY-MM-DD"
				placeholder="No due date" clearable={!required} required={required}
				disabled={!enabled} error={errors.length > 0 ? errors.join(' ') : undefined}
				popoverProps={{ withinPortal: false }} />
		</BoardTheme>
	);
}
