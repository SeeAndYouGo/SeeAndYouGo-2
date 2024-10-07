import React from 'react'
import styled from '@emotion/styled'
import { useState, useCallback } from 'react'
import Cropper from 'react-easy-crop'
import { getCroppedImg, dataURLtoFile } from "../../hooks/useCrop";

const Container = styled.div`
  display: ${props => (props.isOpen ? 'block' : 'none')};
  z-index: 100;
  position: fixed;
  width: 100%;
  height: 100vh;
  top: 0;
  left: 0;
  background-color: rgba(0, 0, 0, 0.5);
`;

const Modal = styled.div`
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
  border-radius: 10px;
  padding: 20px;
  width: 80%;
  max-width: 360px;
  height: 345px;
  overflow: hidden;
  & > .cropperWrapper {
    width: 100%;
    height: 100%;
    max-height: 168px;
    position: relative;
    padding: 20px;
    overflow: hidden !important;
  }
`;

const Button = styled.button`
  flex: 1;
  border-radius: 10px;
  text-align: center;
  height: 35px;
  font-size: 14px;
  border: 1px solid #222;
  background-color: white;
  ${({ className }) => className === 'uploadBtn' && `
    background-color: #222;
    color: white;
  `}
`;

const ImageCropper = ({ setImage, setPrevImage, isOpen, setIsOpen, setImageURL, src, croppedAreaPixels, setCroppedAreaPixels }) => {
  const [crop, setCrop] = useState({ x: 0, y: 0 })
  const [zoom, setZoom] = useState(1)

  const onCropComplete = useCallback((croppedArea, croppedAreaPixel) => {
    setCroppedAreaPixels(croppedAreaPixel);
  }, []);

  const handleCropImage = async () => {
    try {
      const cropped = await getCroppedImg(src, croppedAreaPixels);
      dataURLtoFile(cropped, 'review.jpg', setImage);
      setImageURL(cropped);
      setPrevImage(cropped);
      setIsOpen(false);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <Container isOpen={isOpen}>
      <Modal className='modal modal'>
        <h3>이미지 편집</h3>
        <div className='cropperWrapper'>
          <Cropper
            image={src}
            crop={crop}
            zoom={zoom}
            aspect={16 / 9}
            onCropChange={setCrop}
            onCropComplete={onCropComplete}
            onZoomChange={setZoom}
          />
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: 10, marginTop: 20}}>
          <Button onClick={() => setIsOpen(false)}>취소</Button>
          <Button onClick={handleCropImage} className='uploadBtn'>업로드</Button>
        </div>
      </Modal>
    </Container>
  )
}

export default ImageCropper;