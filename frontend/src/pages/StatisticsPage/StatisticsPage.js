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
  const [statisticsData, setStatisticsData] = useState([]);
  const [statisticsLabel, setStatisticsLabel] = useState([]);
	const [currentTab, setCurrentTab] = useState(0);

	// const createUrl = (restaurantIdx) => config.BASE_URL + "/connection/restaurant" + restaurantIdx + (config.NOW_STATUS === 0 ? ".json" : "");
	const createUrl = (idx) => "http://27.96.131.182/api/statistics/" + idx;

  useEffect(() => {
    const fetchData = async () => {
      try {
        const url = createUrl(currentTab + 1);
        await axios.get(url)
        .then((res) => {
          const datas = res.data.filter((data) => data.time >= '10:00' && data.time <= '15:00');
          const labelData = datas.map(item => item.time);
          const avgData = datas.map(item => item.averageValue);
          setStatisticsLabel(labelData);
          setStatisticsData(avgData);
        });
      } catch (error) {
        console.error(error);
      }
    };
    fetchData();
  }, [currentTab]);

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
          <LineChart statisticsData={statisticsData} statisticsLabel={statisticsLabel} />
        </ChartWrapper>
      </div>
  );
}

export default StatisticsPage;