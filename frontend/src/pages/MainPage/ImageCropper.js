import React, { useCallback, useState } from 'react'
import styled from '@emotion/styled'
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
  height: 450px;
  overflow: hidden;
`;

const CropperContainer = styled.div`
  position: relative;
  width: 100%;
  height: 250px;
  background: #333;
`;

const ZoomBox = styled.div`
  padding: 10px 0;
  width: 100%;
`;

const ZoomInput = styled.input`
  width: 100%;
  height: 4px;
  border-radius: 2px;
  background: #e0e0e0;
  outline: none;
  -webkit-appearance: none;
  
  &::-webkit-slider-thumb {
    -webkit-appearance: none;
    appearance: none;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: #222;
    cursor: pointer;
    border: 2px solid #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  }
  
  &::-moz-range-thumb {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: #222;
    cursor: pointer;
    border: 2px solid #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
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

const ImageCropper = ({ 
  setImage, 
  setPrevImage, 
  isOpen, 
  setIsOpen, 
  setImageURL, 
  src, 
  croppedAreaPixels, 
  setCroppedAreaPixels 
}) => {
  const [crop, setCrop] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const width = 16;
  const height = 9;

  const onCropComplete = useCallback((croppedArea, croppedAreaPixel) => {
    setCroppedAreaPixels(croppedAreaPixel);
  }, [setCroppedAreaPixels]);

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
      <Modal>
        <h3>이미지 편집</h3>
        <CropperContainer>
          <Cropper
            image={src}
            crop={crop}
            zoom={zoom}
            aspect={width / height}
            onCropChange={setCrop}
            onCropComplete={onCropComplete}
            onZoomChange={setZoom}
          />
        </CropperContainer>
        <ZoomBox>
          <ZoomInput
            type="range"
            value={zoom}
            min={1}
            max={3}
            step={0.1}
            aria-labelledby="Zoom"
            onChange={(e) => {
              setZoom(parseFloat(e.target.value));
            }}
          />
        </ZoomBox>
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: 10, marginTop: 10 }}>
          <Button onClick={() => setIsOpen(false)}>취소</Button>
          <Button onClick={handleCropImage} className='uploadBtn'>업로드</Button>
        </div>
      </Modal>
    </Container>
  );
};

export default ImageCropper;