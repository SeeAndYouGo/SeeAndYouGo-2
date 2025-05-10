import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { get } from "../../api";

const Wrapper = styled.div`
  padding: 0 20px 10px 20px;
  width: 100%;
  height: 100%;
  overflow-y: scroll;
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 5px;
  }
  &::-webkit-scrollbar-thumb:hover {
    background: #555;
  }
`;

const MenuItem = styled.div`
  width: 100%;
  padding: 10px 20px;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
  border: solid 1px #f1f1f1;
  border-radius: 10px;
  margin-top: 5px;
`;

const MenuLabel = styled.p`
  padding: 1px 8px;
  border-radius: 5px;
  font-size: 13px;
  font-weight: 400;
  background: #ddd;
  color: #111;
`;

const dateConverter = (date) => {
  const d = new Date(date);
  return `${d.getMonth() + 1}/${d.getDate()}`;
};

const MenuTableModal = ({ idx, onClose }) => {
  const [data, setData] = useState({});

	useEffect(() => {
		const fetchData = async () => {
      const result = await get(`/weekly-menu/restaurant${idx}`);
			return result.data;
		};
		fetchData().then((data) => {
      const menuTypeOrder = {
        BREAKFAST: 0,
        LUNCH: 1,
        DINNER: 2
      }

      const groupedByDate = data.reduce((acc, item) => {
        const date = item.date;
        if (!acc[date]) {
            acc[date] = [];
        }
        acc[date].push(item);
        return acc;
      }, {});

      Object.keys(groupedByDate).forEach(date => {
        groupedByDate[date].sort((a, b) => menuTypeOrder[a.menuType] - menuTypeOrder[b.menuType]);
      });

      setData(groupedByDate)
		});
	}, [idx]);

	return (
    <Wrapper onClick={(e) => e.stopPropagation()}>
      {/* <div 
        style={{float: 'right', cursor: 'pointer', fontWeight: 400, marginRight: '-15px', position: 'absolute'}}
      >
        <span className="material-symbols-outlined">close</span>
      </div> */}
      {Object.entries(data).map(([date, items]) => (
        <div key={date} style={{marginTop: 15}}>
          <p style={{ fontSize: 18 }}>{dateConverter(date)}</p>
          <div>
            {items.map(item => (
              <MenuItem key={item.menuId}>
                <div style={{display: 'flex', gap: 8}}>
                  <p>{item.menuType === "LUNCH" ? '중식' : item.menuType === "BREAKFAST" ? '조식' : '석식'}</p>
                  <MenuLabel>{item.dept === 'STAFF' ? '교직원' : '학생'}</MenuLabel>
                </div>
                <p style={{color: '#999', fontSize: 14, fontWeight: 300, marginTop: 2}}>
                  {item.mainDishList.join(', ')}
                </p>
              </MenuItem>
            ))}
          </div>
        </div>
      ))}
    </Wrapper>
	);
};

export default MenuTableModal;
