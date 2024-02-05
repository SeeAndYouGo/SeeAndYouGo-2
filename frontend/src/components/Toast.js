import React, { useEffect } from 'react';
import styled from '@emotion/styled';
import { useDispatch } from 'react-redux';
import { changeToInitialState } from '../redux/slice/ToastSlice';

const ToastContainer = styled.div`
  position: fixed;
  bottom: 60px;
  left: 50%;
  transform: translateX(-50%);
  max-width: 300px;
  z-index: 1000;
`;

const ToastMessage = styled.div`
  position: relative;
  border-radius: 20px;
  color: #fff;
  padding: 5px 20px;
  font-weight: 300;
  font-size: 14px;
  &.success {
    background-color: #28a745;
  }
  &.error {
    background-color: #f44336;
  }
  &.alert {
    background-color: #777;
  }
`;

const Toast = ({ message, type = 'alert' }) => {
  const dispatch = useDispatch();

  useEffect (() => {
    const timer = setTimeout(() => {
      dispatch(changeToInitialState());
    }, 2000);
    return () => {
      clearTimeout(timer);
    };
  }, [dispatch]);

  return (
    <ToastContainer>
      <ToastMessage className={`toast-content ${type}`}>{message}</ToastMessage>
    </ToastContainer>
  );
}

export default Toast;