import styled from "@emotion/styled";
import { useEffect, useMemo } from "react";
import ReactDOM from "react-dom";
import useClickAway from "../hooks/useClickAway";

const BackgroundDim = styled.div`
	position: fixed;
	top: 0;
	left: 0;
	width: 100vw;
	height: 100vh;
	background-color: rgba(0, 0, 0, 0.5);
	z-index: 1000;
`;

const ModalContainer = styled.div`
	position: fixed;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	padding: 8px;
	background-color: white;
	box-shadow: 0 3px 6px rgba(0, 0, 0, 0.2);
	box-sizing: border-box;
	display: flex;
	flex-direction: column;
	max-height: 80%;
	overflow-y: hidden;
	border-radius: 10px;
`;

const Modal = ({
	children,
	width = 330,
	height,
	visible = false,
	onClose,
	...props
}) => {
	const ref = useClickAway(() => {
		onClose && onClose();
	});

	const handleModalClick = (e) => {
		e.stopPropagation();
	};
	
	const containerStyle = useMemo(
		() => ({
			width,
			height,
		}),
		[width, height]
	);

	const el = useMemo(() => document.createElement("div"), []);

	useEffect(() => {
		document.body.appendChild(el);
		return () => {
			document.body.removeChild(el);
		};
	}, []);

	useEffect(() => {
		if (visible) {
			document.body.style.overflow = "hidden";
		} else {
			document.body.style.overflow = "auto";
		}

		return () => {
			document.body.style.overflow = "auto";
		};
	}, [visible]);

	return ReactDOM.createPortal(
		<BackgroundDim style={{ display: visible ? "block" : "none" }}>
			<ModalContainer
				ref={ref}
				onClick={handleModalClick}
				{...props}
				style={{ ...props.style, ...containerStyle }}
			>
				{children}
			</ModalContainer>
		</BackgroundDim>,
		el
	);
};

export default Modal;
