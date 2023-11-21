import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import { BrowserRouter } from 'react-router-dom';
import ReactGA from 'react-ga4';
const TRACKING_ID = process.env.REACT_APP_GOOGLE_ANALYTICS_TRACKING_ID;
console.log(TRACKING_ID);
ReactGA.initialize(TRACKING_ID);

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  // <React.StrictMode>
  // </React.StrictMode>
  <BrowserRouter>
    <App />
  </BrowserRouter>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
