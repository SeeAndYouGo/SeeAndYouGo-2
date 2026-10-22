import { createSlice } from "@reduxjs/toolkit";

let toastId = 0;

// alert: 회색, 나머지: success ? "success" : "error"
const resolveToastType = ({ type, success, code }) => {
	if (type) return type;
	if (success !== undefined) return success ? "success" : "error";
	if (!code) return "alert";
	return code === "SUCCESS" ? "success" : "error";
};

const isDuplicateToast = (toast, { type, message }) => {
	if (toast.type !== type) return false;
	return toast.message === message;
};

const toastSlice = createSlice({
	name: "toast",
	initialState: { value: { queue: [] } },
	reducers: {
		showToast: (state, action) => {
			const { type, success, code, message } = action.payload;
			if (!message) return;

			const resolvedType = resolveToastType({ type, success, code });

			const isDuplicate = state.value.queue.some((toast) =>
				isDuplicateToast(toast, { type: resolvedType, message })
			);
			if (isDuplicate) return;

			state.value.queue.push({
				id: ++toastId,
				type: resolvedType,
				message,
			});
		},
		removeToast: (state, action) => {
			state.value.queue = state.value.queue.filter(
				(toast) => toast.id !== action.payload
			);
		},
		changeToInitialState: (state) => {
			state.value.queue = [];
		},
	},
});

export const { showToast, removeToast, changeToInitialState } =
	toastSlice.actions;

export default toastSlice.reducer;
