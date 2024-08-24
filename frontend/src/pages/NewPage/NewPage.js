import React from "react";
import TodayMenu from "./TodayMenu";
import ReviewWriteForm from "./ReviewForm";
import { useSelector } from "react-redux";

const NewPage = () => {
  const nowDept = useSelector((state) => state.dept).value;

  return (
    <div>
      <h3 style={{marginBottom: 50}}>
        NEW 오늘의 메뉴, 메뉴 작성 확인하기
      </h3>
      <TodayMenu />
      <ReviewWriteForm restaurantNum={2} deptNum={nowDept}/>
    </div>
  );
}

export default NewPage;