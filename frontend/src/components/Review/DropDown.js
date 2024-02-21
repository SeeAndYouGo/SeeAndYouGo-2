import React from "react";
import styled from "@emotion/styled";
import useDetectClose from "../../hooks/useDetectClose";
import ReviewReport from "./ReviewReport";
import ReviewDelete from "./ReviewDelete";

const DropdownContainer = styled.div`
	text-align: center;
`;

const Menu = styled.div`
	background: white;
	position: absolute;
	top: 26px;
	left: -15px;
	width: 80px;
	box-shadow: 5px 5px 10px rgba(0, 0, 0, 0.2);
	border-radius: 3px;
	opacity: 0;
	visibility: hidden;
	transform: translate(-50%, -20px);
	transition: opacity 0.4s ease, transform 0.4s ease, visibility 0.4s;
	z-index: 9;

	${({ isDropped }) =>
		isDropped &&
		`
			opacity: 1;
			visibility: visible;
			transform: translate(-50%, 0);
			left: -15px;
    `};
`;

const Ul = styled.ul`
	& > li {
		margin-bottom: 10px;
	}

	& > li:first-of-type {
		margin-top: 10px;
	}

	list-style-type: none;
	padding: 0;
	margin: 0;
	display: absolute;
	justify-content: space-between;
	align-items: center;
`;

const ReviewItemOption = styled.span`
	float: right;
	margin-right: -7px;
	color: #777;
	border-radius: 10px;
	font-size: 22px;
	cursor: pointer;
`;

const DropDown = ({ targetId, targetRestaurant, wholeReviewList, setWholeReviewList }) => {
	const [myPageIsOpen, myPageRef, myPageHandler] = useDetectClose(false);

	return (
		<DropdownContainer>
			<ReviewItemOption
				id="reviewOption"
				className="material-symbols-outlined"
				onClick={myPageHandler}
				ref={myPageRef}
			>
				more_vert
			</ReviewItemOption>
			<Menu isDropped={myPageIsOpen}>
				<Ul>
					<li>
						<ReviewReport reportTarget={targetId} />
					</li>
					<li>
						<ReviewDelete deleteTarget={targetId} 
						targetRestaurant={targetRestaurant}
						wholeReviewList={wholeReviewList} setWholeReviewList={setWholeReviewList} />
					</li>
				</Ul>
			</Menu>
		</DropdownContainer>
	);
};

export default DropDown;
