import React, {useEffect, useState} from "react";
import styled from "@emotion/styled";
import axios from "axios";
import LineChart from "./LineChart";
import * as config from "../../config";

const TabMenu = styled.ul`
	color: black;
	font-size: 12px;
	display: flex;
	list-style: none;
	border: solid 1.5px black;
	border-radius: 20px;
	padding: 5px;

	.submenu {
		text-align: center;
		padding: 4px 10px;
		margin: 0 auto;
		border-radius: 20px;
		cursor: pointer;
		@media (min-width: 576px) {
			display: none;
			padding: 4px 17px;
		}
	}
	.submenu.tablet {
		display: none;
		@media (min-width: 576px) {
			display: block;
		}
	}

	.focused {
		background-color: black;
		color: white;
	}
`;

const ChartWrapper = styled.div`
  width: 100%;
  margin: 20px 0 50px 0;
  background: #fff;
  border-radius: 20px;
  padding: 20px;
`;

const restaurantArray = ["1학", "2학", "3학", "상록회관", "생과대"];
const tabletRestaurantArray = [
	"1학생회관",
	"2학생회관",
	"3학생회관",
	"상록회관",
	"생활과학대학",
];


const StatisticsPage = () => {
  const [datas, setDatas] = useState([]);
	const [currentTab, setCurrentTab] = useState(0);

	// const createUrl = (restaurantIdx) => config.BASE_URL + "/connection/restaurant" + restaurantIdx + (config.NOW_STATUS === 0 ? ".json" : "");
	const createUrl = (idx) => "http://27.96.131.182/api/statistics/" + idx;

  useEffect(() => {
    const fetchData = async () => {
      try {
        const url = [createUrl(1), createUrl(2), createUrl(3), createUrl(4), createUrl(5)];
        await axios.all(
          url.map((path) => axios.get(path))
        ).then((res) => {
          setDatas(res.map((data) => data.data));
        }
        );
      } catch (error) {
        console.error(error);
      }
    };
    fetchData();
  }, [])

  const TabMenuUl = () => {
    return (
      <TabMenu>
        {restaurantArray.map((el, index) => (
          <li
            key={index}
            className={index === currentTab ? "submenu focused" : "submenu"}
            onClick={() => setCurrentTab(index)}
          >
            {el}
          </li>
        ))}
        {tabletRestaurantArray.map((el, index) => (
          <li
            key={index}
            className={
              index === currentTab ? "submenu focused tablet" : "submenu tablet"
            }
            onClick={() => setCurrentTab(index)}
          >
            {el}
          </li>
        ))}
      </TabMenu>
    );
  };

  return (
      <div className="App3">
        <TabMenuUl />
        <ChartWrapper>
          {
            datas.length === 0 ? <div>로딩중...</div> :
            <LineChart datas={datas} tab={currentTab} />
          }
        </ChartWrapper>
      </div>
  );
}

export default StatisticsPage;