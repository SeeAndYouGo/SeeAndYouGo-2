import React, { useEffect, useState } from 'react';
import styled from '@emotion/styled';
import { useDispatch } from 'react-redux';
import { showToast } from "../../redux/slice/ToastSlice";
import { erase, get, put } from "../../api/index";

const Button = styled.button`
	background: white;
	border: 1px solid #ddd;
	border-radius: 6px;
	font-size: 16px;
	cursor: pointer;
	font-weight: 500;
  margin-left: 5px;
`;

const EditSection = styled.div`
  overflow: hidden;
  max-height: ${({ isOpen }) => (isOpen ? "500px" : "0")};
  opacity: ${({ isOpen }) => (isOpen ? 1 : 0)};
  transition:
    max-height 0.3s ease,
    opacity 0.3s ease ${({ isOpen }) => (isOpen ? "0s" : "0.2s")},
    margin-top 0.3s ease;
  display: flex;
  gap: 10px;
  margin-top: ${({ isOpen }) => (isOpen ? "10px" : "0")};
`;

const SetDishNamePage = () => {
  const [isAdmin, setIsAdmin] = useState(false);
  const [password, setPassword] = useState("");
  const [buttonDisabled, setButtonDisabled] = useState(false);
	const dispatch = useDispatch();
  
  const handlePasswordChange = (e) => {
    setPassword(e.target.value);
  };

  const handleAdminLogin = () => {
    if (password === process.env.REACT_APP_ADMIN_PASSWORD) {
      setIsAdmin(true);
    } else {
      dispatch(showToast({ contents: "admin", toastIndex: 0 }));
    }
  };

  const handleKeyPress = (e) => {
		if (e.key === "Enter") {
			handleAdminLogin();
		}
	};

  // 백엔드로부터 가져온 데이터
  const [dishList, setDishList] = useState([]);
  const [nowEditId, setNowEditId] = useState(0);
  const [editValue, setEditValue] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      const response = await get("/dish/week");
      const result = response.data;
      return result;
    };
    fetchData().then((data) => {
      setDishList(data);
    }).catch((error) => {
      console.error("에러확인", error);
    });
  }, []);

  // dish 편집 함수
  const handleEditDish = async (dishName, dishId) => {
    if (buttonDisabled) return;
		setButtonDisabled(true);

    try {
      const changeDish = {
        "id": dishId,
        "changeName": dishName
      }
      const response = await put("/dish/name", changeDish); 
      console.log(response, "편집 요청 확인");
      alert("편집되었습니다.");
    } catch (error) {
      alert("편집에 실패했습니다. 다시 시도해주세요.");
      console.error("Error editing dish:", error);
    } finally {
      setButtonDisabled(false);
      setNowEditId(-1);
      setEditValue("");
    }
  };

  // dish 삭제 함수
  const handleDeleteDish = async (dishId) => {
    if (buttonDisabled) return;
		setButtonDisabled(true);

    try {
      const response = await erase(`/dish/${dishId}`);
      console.log(response, "삭제 요청 확인");
      alert("삭제되었습니다.");
    } catch (error) {
      alert("삭제에 실패했습니다. 다시 시도해주세요.");
      console.error("Error deleting dish:", error);
    } finally {
      setButtonDisabled(false);
      setDishList((prev) => prev.filter((dish) => dish.id !== dishId));
    }
  };

  const handleEditClick = (dish) => {
    setNowEditId(dish.id);
    setEditValue(dish.name);
  }

  return (
    <>
      {
        !isAdmin ? (
					<div style={{ margin: "80px auto", width: "360px" }}>
						<label>
							비밀번호:&nbsp;
							<input
								type="password"
								value={password}
								onChange={handlePasswordChange}
								onKeyDown={handleKeyPress}
							/>
						</label>
						<Button onClick={handleAdminLogin}>로그인</Button>
					</div>
				) :
        (
          <div className="AdminPage">
						<div style={{ textAlign: "center", margin: "60px 0 20px" }}>
							<p>비밀 주소입니다. 어떻게 오셨죠?</p>
							<p>전송 클릭 시 확인창이 등장합니다.</p>
						</div>
            {
              dishList.map((dish) => {
                return <div key={dish.id} style={{ margin: "20px 0", position: "relative", display: "flex", fontSize: "14px" }}>
                    <div style={{ width: "160px" }}>
                      <p>이름: {dish.name}</p>
                      <p>ID: {dish.id}</p>
                    </div>
                    <div>
                      {
                        <Button onClick={() => {
                          if ( window.confirm("해당 Dish를 삭제합니다.\n다시한번 확인해주세요!\n정말로 삭제하시겠습니까?") ) {
                            handleDeleteDish(dish.id);
                          } else {
                            return;
                          }
                        }}>
                          삭제
                        </Button>
                      }
                      {
                        nowEditId === dish.id ? (
                          <Button onClick={() => {
                            setNowEditId(-1);
                          }}>
                            편집 닫기
                          </Button>
                        ) : (
                          <Button onClick={() => {
                            handleEditClick(dish);
                          }}>
                            편집
                          </Button>
                        )
                      }
                      <EditSection isOpen={nowEditId === dish.id}>
                        <input
                          type="text"
                          onChange={(e) => {
                            setEditValue(e.target.value)}
                          }
                          value={editValue}
                          style={{ padding: "4px", width: "120px",marginLeft: "5px" }}
                        />
                        <Button onClick={() => {
                          if ( window.confirm("해당 Dish를 편집합니다.\n편집하시겠습니까?") ) {
														handleEditDish(editValue, dish.id);
													} else {
														return;
													}
                        }}>
                          전송
                        </Button>
                      </EditSection>
                    </div>
                  </div>
              })
            }
          </div>
        )
      }
    </>
  );
}

export default SetDishNamePage;