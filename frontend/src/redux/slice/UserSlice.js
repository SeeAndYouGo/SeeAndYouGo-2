import { createSlice } from '@reduxjs/toolkit';
import { PURGE } from 'redux-persist';

const initialStateValue = {
  value: {
    token: "",
    nickname: "",
    loginState: false
  }
};

const UserSlice = createSlice({
  name: 'user',
  initialState: { value: initialStateValue },
  reducers: {
    login: (state, action) => {
      console.log(action);
      state.value = action.payload;
    },
    logout: (state) => {
      state.value = initialStateValue;
    },
    setNickname: (state, action) => {
      console.log(action);
      state.value.nickname = action.payload;
    }
  },
  extraReducers: builder => {
    builder.addCase(PURGE, (state) => {
      state.value = initialStateValue;
    });
  },
});

export const { login, logout, setNickname } = UserSlice.actions;

export default UserSlice.reducer;
