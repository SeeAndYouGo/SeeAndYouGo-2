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


// 리뷰작성시 dept와 type이 아닌 menuId만 필요하기에 menuId slice를 생성해주시기로