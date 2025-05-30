import { useEffect, useRef } from "react";
import styled from "@emotion/styled";

const MapContainer = styled.div`
	width: 100%;
	display: inline-block;
	margin-left: 5px;
	margin-right: 5px;
`;

const RestaurantLocation = [
  [36.367873, 127.343175], // 1학생회관
  [36.366034, 127.345821], // 2학생회관
  [36.371531, 127.344679], // 3학생회관
  [36.368539, 127.350378], // 상록회관
  [36.375791, 127.342973], // 생활과학대
  [36.372584, 127.346340], // 기숙사식당
]

const KakaoMap = ({ restaurantId, modalOpen }) => {
  const mapRef = useRef(null);
  const markerRef = useRef(null);
  const mapInstanceRef = useRef(null);

  useEffect(() => {
    const kakaoMapKey = process.env.REACT_APP_KAKAOMAP_KEY;
    const scriptId = "kakao-map-script";

    const initializeMap = () => {
      const [lat, lng] = RestaurantLocation[restaurantId - 1];
      const center = new window.kakao.maps.LatLng(lat, lng);

      const map = new window.kakao.maps.Map(mapRef.current, {
        center,
        level: 3,
        draggable: true,
      });

      const marker = new window.kakao.maps.Marker({
        position: center,
        map,
      });

      map.setDraggable(true);
      mapInstanceRef.current = map;
      markerRef.current = marker;

      setTimeout(() => {
        map.relayout();
        map.setCenter(center);
      }, 300);

    };

    if (document.getElementById(scriptId)) {
      if (window.kakao && window.kakao.maps) {
        initializeMap();
      }
      return;
    }

    const script = document.createElement("script");
    script.id = scriptId;
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoMapKey}&autoload=false&libraries=services,clusterer,drawing`;
    script.async = true;
    script.onload = () => {
      window.kakao.maps.load(() => {
        initializeMap();
      });
    };
    document.head.appendChild(script);
  }, []);

  useEffect(() => {
    if (!mapInstanceRef.current || !markerRef.current) return;

    const observer = new ResizeObserver(() => {
      mapInstanceRef.current.relayout();
      const [lat, lng] = RestaurantLocation[restaurantId - 1];

      const newCenter = new window.kakao.maps.LatLng(lat, lng);
      mapInstanceRef.current.setCenter(newCenter);
      markerRef.current.setPosition(newCenter);
    });

    observer.observe(mapRef.current);

    setTimeout(() => {
      mapInstanceRef.current.relayout();
    }, 300);

    return () => {
      observer.disconnect();
    };
  }, [restaurantId, modalOpen]);


	return (
		<MapContainer>
			<div ref={mapRef} style={{ width: "270px", height: "300px" }}></div>
		</MapContainer>
	);
};

export default KakaoMap;
