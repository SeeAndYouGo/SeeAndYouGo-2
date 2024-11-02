import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import * as config from "../../config";

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
  background: #111;
  border-radius: 5px;
  color: #fff;
  font-size: 13px;
  font-weight: 400;
  ${props => props.isPrice && `
    background: #ddd;
    color: #111;
  `}
`;

const dateConverter = (date) => {
  const d = new Date(date);
  return `${d.getMonth() + 1}/${d.getDate()}`;
};

const MenuTableModal = ({ idx, onClose }) => {
  const [data, setData] = useState({});

	useEffect(() => {
		const fetchData = async () => {
			const url =
				config.BASE_URL +
				`/weekly-menu/restaurant${idx}` +
				(config.NOW_STATUS === 0 ? ".json" : "");

			const res = await fetch(url, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "GET",
			});
			const result = await res.json();
			return result;
		};
		fetchData().then((data) => {
      const groupedByDate = data.reduce((acc, item) => {
        const date = item.date;
        if (!acc[date]) {
            acc[date] = [];
        }
        acc[date].push(item);
        return acc;
      }, {});
      setData(groupedByDate)
		});
	}, [idx]);

	return (
    <div style={{padding: '0 20px 10px 20px'}}>
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
                  <MenuLabel>{item.dept === 'STAFF' ? '교직원식당' : '학생식당'}</MenuLabel>
                  <MenuLabel isPrice={true}>{item.price}</MenuLabel>
                </div>
                <p style={{color: '#999', fontSize: 14, fontWeight: 300, marginTop: 2}}>
                  {item.mainDishList.join(', ')}
                </p>
              </MenuItem>
            ))}
          </div>
        </div>
      ))}
   </div>
	);
};

export default MenuTableModal;
