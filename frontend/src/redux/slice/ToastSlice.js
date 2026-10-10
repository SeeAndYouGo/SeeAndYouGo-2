import { createSlice } from "@reduxjs/toolkit";

let toastId = 0;

// success와 error 둘 다 코드가 같을 때만 메시지로 중복을 판단하기
const isDuplicateToast = (toast, { code, message }) => {
	if (toast.code !== code) return false;
	return toast.message === message;
};

const toastSlice = createSlice({
	name: "toast",
	initialState: { value: { queue: [] } },
	reducers: {
		showToast: (state, action) => {
			const { code = null, message } = action.payload;
			if (!message) return;

			const isDuplicate = state.value.queue.some((toast) =>
				isDuplicateToast(toast, { code, message })
			);
			if (isDuplicate) return;

			state.value.queue.push({
				id: ++toastId,
				code,
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
