import { createSlice } from "@reduxjs/toolkit";

const initialStateValue = {
	contents: "",
	toastIndex: null,
};

const toastSlice = createSlice({
	name: "toast",
	initialState: { value: initialStateValue },
	reducers: {
		showToast: (state, action) => {
			state.value = action.payload;
		},
		changeToInitialState: (state) => {
			state.value = initialStateValue;
		},
	},
});

export const { showToast, changeToInitialState } = toastSlice.actions;

export default toastSlice.reducer;
