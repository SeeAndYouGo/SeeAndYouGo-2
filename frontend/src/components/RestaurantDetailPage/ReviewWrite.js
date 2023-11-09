import styled from "@emotion/styled";
import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import StarsRating from "react-star-rate";

const ReviewWriteContainer = styled.form`
	width: 100%;
	background: #fff;
	padding: 15px;
	border-radius: 20px;
	margin: 10px 0 20px 0;
	float: left;

	& .rs-picker-toggle-placeholder,
	& .rs-picker-search-bar-input {
		font-size: 12px;
		font-weight: 400;
	}
	& .rs-picker-cascader-menu-items {
		font-size: 12px;
	}
`;
const ReviewStarRating = styled.span`
	float: left;
	color: #d9d9d9;
	margin-top: -5px;
	margin-left: 10px;
	> ul {
		font-size: 20px;
	}
	> ul > li {
		margin-right: 3px;
	}
`;
const ReviewWriteInput = styled.input`
	color: #999;
	border: 1px solid #d9d9d9;
	border-radius: 10px;
	padding: 0 10px;
	padding-right: 35px;
	height: 35px;
	outline: none;
	float: right;
	font-size: 12px;
	font-weight: 400;
	width: 100%;

	&::placeholder {
		font-weight: 400;
		font-size: 12px;
	}
`;

const ReviewWriteButton = styled.button`
	width: 100%;
	margin-top: 10px;
	font-size: 12px;
	background: #d9d9d9;
	color: "#777";
	border-radius: 10px;
	border: none;
	height: 30px;
	float: left;
	font-weight: 400;
	cursor: pointer;
`;
const ReviewWriteNameChekbox = styled.input`
	float: left;
	width: 15px;
	height: 15px;
	margin-left: 5px;
	margin-right: 10px;
	margin-top: 3px;
	position: relative;
	top: 50%;
	transform: translateY(-50%);
`;

const ReviewWriteForm = ({restaurantName, deptName, nowMainMenu}) => {
	const [checked, setChecked] = useState(false);
	const [starVal, setStarVal] = useState(0);
	const [writerName, setwriterName] = useState("");
	const [comment, setComment] = useState("");

	const ReviewSubmit = async (e) => {
		e.preventDefault();

        const myObject = {
            restaurant: restaurantName,
            dept: deptName,
            menuName: nowMainMenu,
            rate: starVal,
            writer: writerName === "" ? "익명" : writerName,
            comment: comment
        }

		// const formdata = new FormData();
		// // 식당이름 restaurant
		// formdata.append("restaurant", restaurantName);
		// // 식당구분 dept
		// formdata.append("dept", deptName);
        // // 메뉴이름 menuName
		// formdata.append("menuName", nowMainMenu);
		// // 평점 rate
		// formdata.append("rate", starVal);
        // // 작성자 writer
		// formdata.append("writer", writerName);
		// // 리뷰 comment
		// formdata.append("comment", comment);

		// var requestOptions = {
		// 	method: "POST",
		// 	body: formdata,
		// 	redirect: "follow",
		// };
        
        // console.log(formdata);

		fetch("http://27.96.131.182/api/review", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(myObject),
        })
			.then((response) => response.json())
			.then(() => { 
                alert("리뷰가 등록되었습니다.");
                window.location.reload();
            })
			.catch((error) => console.log("error", error));
	};

	return (
		<ReviewWriteContainer>
			<div style={{ width: "50%", float: "left" }}>
				<p style={{ margin: "0", float: "left", fontSize: 15 }}>별점</p>
				<ReviewStarRating>
					<StarsRating
						value={starVal}
						onChange={(value) => {
							setStarVal(value);
						}}
					/>
				</ReviewStarRating>
			</div>
			<div style={{ width: "100%", float: "left", height: 30 }}>
				<p
					style={{
						margin: "0px 0px 5px 0px",
						float: "left",
						fontSize: 15,
						lineHeight: "35px",
					}}
				>
					익명
				</p>

				<ReviewWriteNameChekbox
					type="checkbox"
					onChange={() => {
						setChecked(!checked);
					}}
				/>

				{checked ? (
					<ReviewWriteInput
						disabled
						value={"익명"}
						type="text"
                        onClick={() => {
                            setwriterName("익명");
                        }}
						maxLength={6}
						style={{
							height: 30,
							float: "left",
							width: "30%",
							lineHeight: 25,
							paddingRight: 0,
						}}
					/>
				) : (
					<ReviewWriteInput
						type="text"
						onChange={(name) => {
							setwriterName(name.target.value);
						}}
						maxLength={6}
						placeholder={"닉네임"}
                        value={writerName}
						style={{
							height: 30,
							float: "left",
							width: "30%",
							lineHeight: 25,
							paddingRight: 0,
						}}
					/>
				)}
			</div>
			<div style={{ width: "100%", float: "left", marginTop: 5 }}>
				<div
					style={{
						position: "relative",
						width: "100%",
						float: "left",
					}}
				>
					<ReviewWriteInput
						type="text"
						onChange={(val) => setComment(val.target.value)}
						placeholder="리뷰를 남겨주세요 :)"
					/>
				</div>
				{starVal !== 0 ? (
					<ReviewWriteButton onClick={ReviewSubmit}>
						작성
					</ReviewWriteButton>
				) : (
					<ReviewWriteButton disabled onClick={ReviewSubmit}>
						작성
					</ReviewWriteButton>
				)}
			</div>
		</ReviewWriteContainer>
	);
};

const ReviewWrite = ({restaurantName, deptName, nowMainMenu}) => {
	return (
		<div style={{ float: "left", marginTop: 20 }}>
			<p style={{ fontSize: 18, margin: 0 }}>오늘의 메뉴 리뷰 남기기</p>
			<ReviewWriteForm restaurantName={restaurantName} deptName={deptName} nowMainMenu={nowMainMenu}/>
		</div>
	);
};

export default ReviewWrite;
