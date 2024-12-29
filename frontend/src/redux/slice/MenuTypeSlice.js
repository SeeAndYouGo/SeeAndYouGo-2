import { createSlice } from "@reduxjs/toolkit";

const initialState = 1;

const menuTypeSlice = createSlice({
  name: "menuType",
  initialState: { value: initialState },
  reducers: {
    changeMenuType: (state, action) => {
      state.value = action.payload;
    },
    changeToInitialState: (state) => {
      state.value = initialState;
    },
  },
});

export const { changeMenuType, changeToInitialState } = menuTypeSlice.actions;

export default menuTypeSlice.reducer;