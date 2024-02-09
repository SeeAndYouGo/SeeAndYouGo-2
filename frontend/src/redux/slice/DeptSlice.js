import { createSlice } from "@reduxjs/toolkit";

const initialState = 1;

const deptSlice = createSlice({
  name: "dept",
  initialState: { value: initialState },
  reducers: {
    changeDept: (state, action) => {
      state.value = action.payload;
    },
    changeToInitialState: (state) => {
      state.value = initialState;
    },
  },
});

export const { changeDept, changeToInitialState } = deptSlice.actions;

export default deptSlice.reducer;