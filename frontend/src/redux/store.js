import { configureStore } from '@reduxjs/toolkit'
import storage from 'redux-persist/lib/storage';
import { persistReducer } from 'redux-persist';
import { combineReducers } from 'redux'
import UserSlice from './slice/UserSlice';
import ToastSlice from './slice/ToastSlice';
import DeptSlice from './slice/DeptSlice';

const reducers = combineReducers({
    user: UserSlice,
    toast: ToastSlice,
    dept: DeptSlice,
});

const persistConfig = {
  key : 'root',
  storage,
  whitelist: ['user']
};

const persistedReducer = persistReducer(persistConfig, reducers);

const store = configureStore({
  reducer: persistedReducer,
  middleware: (getDefaultMiddleware) => getDefaultMiddleware({
    serializableCheck: false,
  }),
});

export default store;