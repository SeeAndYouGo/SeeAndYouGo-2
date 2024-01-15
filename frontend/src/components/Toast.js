import React, { useEffect } from 'react';
import styled from '@emotion/styled';

const ToastContainer = styled.div`
  position: fixed;
  bottom: 60px;
  left: 50%;
  transform: translateX(-50%);
  max-width: 300px;
  border-solid: 1px solid red;
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

const Toast = ({ message, type = 'alert', setToast }) => {

  useEffect (() => {
    const timer = setTimeout(() => {
      setToast(false);
    }, 2000);
    return () => {
      clearTimeout(timer);
    };
  }, [setToast]);

  return (
    <ToastContainer>
      <ToastMessage className={`toast-content ${type}`}>{message}</ToastMessage>
    </ToastContainer>
  );
}

export default Toast;