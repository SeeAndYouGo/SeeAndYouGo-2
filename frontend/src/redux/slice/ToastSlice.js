import { createSlice } from "@reduxjs/toolkit";

const initialState = null;

const toastSlice = createSlice({
	name: "toast",
	initialState: { value: initialState },
	reducers: {
		changeToastIndex: (state, action) => {
			state.value = action.payload;
		},
		changeToInitialState: (state) => {
			state.value = initialState;
		},
	},
});

export const { changeToastIndex, changeToInitialState } = toastSlice.actions;

export default toastSlice.reducer;
