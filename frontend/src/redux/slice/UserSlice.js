import { createSlice } from '@reduxjs/toolkit';
import { PURGE } from 'redux-persist';

const initialStateValue = {
  token: "",
  nickname: "",
  loginState: false,
  selectedRestaurant: 1,
};

const UserSlice = createSlice({
  name: 'user',
  initialState: { value: initialStateValue },
  reducers: {
    login: (state, action) => {
      state.value = action.payload;
    },
    logout: (state) => {
      state.value = initialStateValue;
    },
    setNickname: (state, action) => {
      state.value.nickname = action.payload;
    },
    setSelectedRestaurant: (state, action) => {
      state.value.selectedRestaurant = action.payload;
    }
  },
  extraReducers: builder => {
    builder.addCase(PURGE, (state) => {
      state.value = initialStateValue;
    });
  },
});

export const { login, logout, setNickname, setSelectedRestaurant } = UserSlice.actions;

export default UserSlice.reducer;
