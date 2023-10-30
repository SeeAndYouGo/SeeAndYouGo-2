import { SelectPicker } from 'rsuite';

const data = ['Eugenia', 'Bryan', 'Linda', 'Nancy', 'Lloyd', 'Alice', 'Julia', 'Albert'].map(
    item => ({ label: item, value: item })
  );

const LoadingPage = () => {
    return (
        <div className="loading-page">
            <h1>죄송합니다...</h1>
            <h2>수정 작업중입니다.</h2>

            <SelectPicker data={data} style={{ width: 224 }} />
        </div>
    );
};

export default LoadingPage;