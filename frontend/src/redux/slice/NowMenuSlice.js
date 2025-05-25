import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  mainMenuList: [],
  menuId: 0,
  menuType: 'BREAKFAST',
}

const nowMenuSlice = createSlice({
  name: "nowMenuInfo",
  initialState: { value: initialState },
  reducers: {
    changeMenuInfo: (state, action) => {
      state.value = action.payload;
    },
    changeToInitialState: (state) => {
      state.value = initialState;
    },
  },
});

export const { changeMenuInfo, changeToInitialState } = nowMenuSlice.actions;

export default nowMenuSlice.reducer;