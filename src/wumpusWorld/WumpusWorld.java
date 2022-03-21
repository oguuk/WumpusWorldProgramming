package wumpusWorld;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class WumpusWorld {

	public static Room[][] rooms = new Room[4][4];
	public static int[] pathx = new int[1000];
	public static int[] pathy = new int[1000];
	public static int pathindex = 0;
	public static int nextx, nexty;
	public static int wumx, wumy;
	public static int secwumx, secwumy;
	public static int goldx, goldy;
	public static int prevx, prevy;
	public static Agent ai = new Agent();
	public static int arrow = 3;
	public static int direction = 0; // 0:right, 1: forward, 2: left, 3: back

	public static void main(String[] args) {

		//1. wumpus world의 환경 설정 초기화

		//출발점을 제외한 방에 pits, wumpus, gold가 존재
		//wumpus, gold, pits는 random하게 존재한다.
		//단, wumpus와 gold는 pits와 같은 방에 존재하지 않는다.
		//출발점이 pits로 둘러싸여  있을 때는 초기화를 다시 한다. 

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				rooms[i][j] = new Room();
				rooms[i][j].x = i + 1;
				rooms[i][j].y = j + 1;
			}
		}
		//하나의 gold의  위치를 random하게 잡는다.
		locategold();
		//4x4=16개 방의 15%는 2.4개 이므로 2개의 wumpus가 존재한다.
		locatewupus();

		//4x4=16개 방의 15%는 2.4개 이므로 2개의 pits가 존재한다.
		//이미 wumpus와 gold와 pits는 서로 다른 위치에 있다. 
		locatepits();

		//초기화가 끝났으면 pits,wumpus,gold에 의한 각 방의 breeze,stench,glitter를 설정한다.
		// gold가 있는 방의 glitter 설정
		setglitter(goldx, goldy);
		//wumpus에 의한 옆 방의 stench 설정
		setstench(wumx, wumy);
		setstench(secwumx, secwumy);
		//pit에 의한 옆 방의 breeze 설정
		setbreeze();

		//text 파일에 초기화된 상태 출력
		outputstate(); // pit, gold, wumpus의 위치 출력
		outputpercept(); // breeze, glitter, stench 상태 출력
		//여기서부터
		//2. gold를 찾기 위해 이동

		//gold를 찾으면 성공한다.
		boolean success = false;
		boolean fail = false;
		int currentx = 0, currenty = 0;

		int failcause = 0;
		
		prevx = currentx;
		prevy = currenty;
		for (int i = 0; i < 100; i++) {
			pathx[i] = -1;
			pathy[i] = -1;
		}
		while (!success) {
			pathx[pathindex] = currentx;
			pathy[pathindex++] = currenty;
			rooms[currentx][currenty].visit = true;

			if (rooms[currentx][currenty].perceive.glitter) {
				success = findgold(currentx,currenty);
			}
			if (rooms[currentx][currenty].pit) {
				failcause = 1;
				fail = true;
			} else if (rooms[currentx][currenty].wumpus) {
				failcause = 2;
				fail = true;
			}

			if (success) {
				outputpath(1, 0);
				System.out.println("SUCCESS : The Agent Grabbed the GOLD!!!!!");
				break;
			}
			if (fail) {
				if (failcause == 1) {
					outputpath(2, 1);
					ai.infer[currentx][currenty].p = true; // wumpus에 의해서 agent가 죽었다면, 죽은 위치에 wumpus가 있음을 기억한다.
					System.out.println("FAIL : The Agent Died because of Pit");
				} else if (failcause == 2) {
					outputpath(2, 2);
					ai.infer[currentx][currenty].w = true; // pit에 의해서 agent가 죽었다면, 죽은 위치에 pit가 있음을 기억한다.
					System.out.println("FAIL : The Agent Died because of Wumpus");
				}
				// break -> continue : 실패 시에도 다시 처음 위치에서 재시작
				currentx = 0;
				currenty = 0;
				init_toRestart(currentx, currenty);
				fail = false;
				continue;
				// break;
			}

			if ((!rooms[currentx][currenty].perceive.breeze) && (!rooms[currentx][currenty].perceive.stench)) {
				setok(currentx, currenty);
			}
			if (rooms[currentx][currenty].perceive.breeze) { // not //else if -> if
				setpit(currentx, currenty);
			}
			if (rooms[currentx][currenty].perceive.stench) { // not //else if -> if
				setwum(currentx, currenty);
			}

			//다음 위치로 이동... 
			findnext(currentx, currenty);
			prevx = currentx;
			prevy = currenty;
			currentx = nextx;
			currenty = nexty;

		} // end of while*/
	}// end of main

	public static void setok(int x, int y) {
		ai.infer[x][y].ok = true;
		if (x != 0)
			ai.infer[x - 1][y].ok = true;
		if (x != 3)
			ai.infer[x + 1][y].ok = true;
		if (y != 0)
			ai.infer[x][y - 1].ok = true;
		if (y != 3)
			ai.infer[x][y + 1].ok = true;
	}// end of setok

	public static void setpit(int x, int y) {
		ai.infer[x][y].ok = true;
		if (x != 0) {
			if (!rooms[x - 1][y].visit)
				ai.infer[x - 1][y].pp = true;
		}
		if (x != 3) {
			if (!rooms[x + 1][y].visit)
				ai.infer[x + 1][y].pp = true;
		}
		if (y != 0) {
			if (!rooms[x][y - 1].visit)
				ai.infer[x][y - 1].pp = true;
		}
		if (y != 3) {
			if (!rooms[x][y + 1].visit)
				ai.infer[x][y + 1].pp = true;
		}
	}// end of setpit

	public static void setwum(int x, int y) {
		ai.infer[x][y].ok = true;
		if (x != 0) {
			if (!rooms[x - 1][y].visit)
				ai.infer[x - 1][y].wp = true;
		}
		if (x != 3) {
			if (!rooms[x + 1][y].visit)
				ai.infer[x + 1][y].wp = true;
		}
		if (y != 0) {
			if (!rooms[x][y - 1].visit)
				ai.infer[x][y - 1].wp = true;
		}
		if (y != 3) {
			if (!rooms[x][y + 1].visit)
				ai.infer[x][y + 1].wp = true;
		}
	}// end of setwum

	public static void findnext(int x, int y) {
		// 진행 방향으로 pit 또는 wumpus가 있는지 확인한다.
		boolean isPit = false;
		boolean isWumpus = false;
		switch (direction) {
		case 0:
			// right
			if (x != 3 && ai.infer[x + 1][y].p) {
				isPit = true;
			}
			if (x != 3 && ai.infer[x + 1][y].w) {
				isWumpus = true;
			}
			break;
		case 1:
			// forward
			if (y != 3 && ai.infer[x][y + 1].p) {
				isPit = true;
			}
			if (y != 3 && ai.infer[x][y + 1].w) {
				isWumpus = true;
			}
			break;
		case 2:
			// left
			if (x != 0 && ai.infer[x - 1][y].p) {
				isPit = true;
			}
			if (x != 0 && ai.infer[x - 1][y].w) {
				isWumpus = true;
			}
			break;
		case 3:
			// back
			if (y != 0 && ai.infer[x][y - 1].p) {
				isPit = true;
			}
			if (y != 0 && ai.infer[x][y - 1].w) {
				isWumpus = true;
			}
			break;
		}
		if ((!rooms[x][y].perceive.breeze) && (!rooms[x][y].perceive.stench)) // breeze와 stench가 안느껴졌다.
		{
			boolean pass = false;
			while (!pass) {
				// 0:right, 1: forward, 2: left, 3: back
				switch (direction) {
				case 0: // heading to right
					if (x != 3) {
						nextx = x + 1;
						nexty = y;
						pass = true;
					}

					break;
				case 1: // heading to forward
					if (y != 3) {
						nextx = x;
						nexty = y + 1;
						pass = true;
					}
					break;
				case 2: // heading to left
					if (x != 0) {
						nextx = x - 1;
						nexty = y;
						pass = true;
					}

					break;
				case 3: // heading to back
					if (y != 0) {
						nextx = x;
						nexty = y - 1;
						pass = true;
					}

					break;
				}// end switch
				int ran = (int) (Math.random() * 2);
				switch (direction) {
				case 0:
					// right(동)
					if (x == 3) {
						if (y != 3 && ran == 1)
							direction = 1; // 맨 위에 있지 않으면
						else if (y != 0 && ran == 0)
							direction = 3; // 맨 아래에 있지 않으면
						else if (y == 3)
							direction = 3; // 맨 위에 있다면
						else if (y == 0)
							direction = 1; // 맨 아래에 있다면
					}
					break;
				case 1:
					// 1: forward(북)
					if (y == 3) {
						if (x != 3 && ran == 1)
							direction = 0; // 맨 오른쪽에 있지 않으면
						else if (x != 0 && ran == 0)
							direction = 2; // 맨 왼쪽에 있지 않으면
						else if (x == 3)
							direction = 2; // 맨 오른쪽에 있다면
						else if (x == 0)
							direction = 0; // 맨 왼쪽에 있다면
					}
					break;
				case 2:
					// 2: left(서)
					if (x == 0) {
						if (y != 3 && ran == 1)
							direction = 1; // 맨 위에 있지 않으면
						else if (y != 0 && ran == 0)
							direction = 3; // 맨 아래에 있지 않으면
						else if (y == 3)
							direction = 3; // 맨 위에 있다면
						else if (y == 0)
							direction = 1; // 맨 아래에 있다면
					}
					break;
				case 3:
					// 3: back(남)
					if (y == 0) {
						if (x != 3 && ran == 1)
							direction = 0; // 맨 오른쪽에 있지 않으면
						else if (x != 0 && ran == 0)
							direction = 2; // 맨 왼쪽에 있지 않으면
						else if (x == 3)
							direction = 2; // 맨 오른쪽에 있다면
						else if (x == 0)
							direction = 0; // 맨 왼쪽에 있다면
					}
					break;
				}// end switch
			}
		}
		// 냄새가 날 경우나 바람을 느꼈을 경우 -> 진행방향으로 진행
		else if ((rooms[x][y].perceive.breeze) || (rooms[x][y].perceive.stench)) {
			int pre_direction = direction;
			// 만약 벽을 보고 있다면
			if (x == 0 || x == 3 || y == 0 || y == 3) {
				int ran = (int) (Math.random() * 2);
				switch (direction) {
				case 0:
					// right(동)
					if (x == 3) {
						if (y != 3 && ran == 1)
							direction = 1; // 맨 위에 있지 않으면
						else if (y != 0 && ran == 0)
							direction = 3; // 맨 아래에 있지 않으면
						else if (y == 3)
							direction = 3; // 맨 위에 있다면
						else if (y == 0)
							direction = 1; // 맨 아래에 있다면
					}
					break;
				case 1:
					// 1: forward(북)
					if (y == 3) {
						if (x != 3 && ran == 1)
							direction = 0; // 맨 오른쪽에 있지 않으면
						else if (x != 0 && ran == 0)
							direction = 2; // 맨 왼쪽에 있지 않으면
						else if (x == 3)
							direction = 2; // 맨 오른쪽에 있다면
						else if (x == 0)
							direction = 0; // 맨 왼쪽에 있다면
					}
					break;
				case 2:
					// 2: left(서)
					if (x == 0) {
						if (y != 3 && ran == 1)
							direction = 1; // 맨 위에 있지 않으면
						else if (y != 0 && ran == 0)
							direction = 3; // 맨 아래에 있지 않으면
						else if (y == 3)
							direction = 3; // 맨 위에 있다면
						else if (y == 0)
							direction = 1; // 맨 아래에 있다면
					}
					break;
				case 3:
					// 3: back(남)
					if (y == 0) {
						if (x != 3 && ran == 1)
							direction = 0; // 맨 오른쪽에 있지 않으면
						else if (x != 0 && ran == 0)
							direction = 2; // 맨 왼쪽에 있지 않으면
						else if (x == 3)
							direction = 2; // 맨 오른쪽에 있다면
						else if (x == 0)
							direction = 0; // 맨 왼쪽에 있다면
					}
					break;
				}// end switch
			}
			if (pre_direction != direction) {
				switch (direction) {
				case 0:
					// right
					if (x != 3 && ai.infer[x + 1][y].p) {
						isPit = true;
					}
					if (x != 3 && ai.infer[x + 1][y].w) {
						isWumpus = true;
					}
					break;
				case 1:
					// forward
					if (y != 3 && ai.infer[x][y + 1].p) {
						isPit = true;
					}
					if (y != 3 && ai.infer[x][y + 1].w) {
						isWumpus = true;
					}
					break;
				case 2:
					// left
					if (x != 0 && ai.infer[x - 1][y].p) {
						isPit = true;
					}
					if (x != 0 && ai.infer[x - 1][y].w) {
						isWumpus = true;
					}
					break;
				case 3:
					// back
					if (y != 0 && ai.infer[x][y - 1].p) {
						isPit = true;
					}
					if (y != 0 && ai.infer[x][y - 1].w) {
						isWumpus = true;
					}
					break;
				}
			}
			if (!isWumpus && !isPit) {
				switch (direction) {
				case 0: // heading to right
					if (x != 3) {
						nextx = x + 1;
						nexty = y;
					}
					break;
				case 1: // heading to forward
					if (y != 3) {
						nextx = x;
						nexty = y + 1;
					}
					break;
				case 2: // heading to left
					if (x != 0) {
						nextx = x - 1;
						nexty = y;
					}
					break;
				case 3: // heading to back
					if (y != 0) {
						nextx = x;
						nexty = y - 1;
					}
					break;
				}// end switch
			}
		}

		// 진행 방향으로 pit이 존재한다면
		if (isPit) {
			int ran;
			switch (direction) {
			case 0:
				// right(동)
				if (y != 3 && y != 0 && x != 0) { // 오른쪽을 제외하고 모든 방향으로 진행이 가능할 때
					ran = (int) (Math.random() * 3);
					direction = ran + 1;
					return;
				} else if (y == 3 && x != 0) { // 아래와 왼쪽으로 진행이 가능할 때
					ran = (int) (Math.random() * 2);
					direction = ran + 2;
				} else if (y == 0 && x != 0) { // 위와 왼쪽으로 진행이 가능할 때
					ran = (int) (Math.random() * 2);
					direction = ran + 1;
				} else if (y != 3 && y != 0 && x == 0) { // 위와 아래로 진행이 가능할 때
					ran = (int) (Math.random() * 2);
					direction = (ran == 0 ? 1 : 3);
				} else if (y == 3 && x == 0) { // 아래쪽으로만 갈 수 있을 때
					direction = 3;
				}

				break;
			case 1:
				// 1: forward(북)
				if (y != 0 && x != 0 && x != 3) { // 위쪽을 제외하고 모든 방향으로 진행이 가능할 때
					ran = (int) (Math.random() * 3);// 0 2 3
					direction = (ran == 2 ? 0 : ran + 2);
					return;
				} else if (y == 0 && x != 0 && x != 3) { // 오른쪽과 왼쪽으로 진행이 가능할 때
					ran = (int) (Math.random() * 2);// 0 2
					direction = (ran == 0 ? 0 : 2);
				} else if (y != 0 && x == 0) { // 아래쪽와 오른쪽으로 진행이 가능할 때
					ran = (int) (Math.random() * 2);// 0 3
					direction = (ran == 0 ? 0 : 3);
				} else if (y != 0 && x == 3) { // 아래쪽와 왼쪽으로 진행이 가능할 때
					ran = (int) (Math.random() * 2);// 2,3
					direction = ran + 2;
				} else if (x == 3 && y == 0) {
					direction = 2;
				}
				break;
			case 2:
				// 2: left(서)
				if (y != 3 && y != 0 && x != 3) { // 왼쪽을 제외하고 모든 방향으로 진행이 가능할 때
					ran = (int) (Math.random() * 3); // 0 1 3
					direction = (ran == 2 ? 3 : ran);
					return;
				} else if (y == 3 && x != 3) { // 아래쪽와 오른쪽으로 진행이 가능할 때
					ran = (int) (Math.random() * 2);
					direction = (ran == 0 ? 0 : 3); // 0 3
				} else if (y == 0 && x != 3) { // 위와 오른쪽으로 진행이 가능할 때
					ran = (int) (Math.random() * 2);
					direction = ran; // 0 1
				} else if (y != 3 && y != 0 && x == 3) { // 위와 아래로 진행이 가능할 때
					ran = (int) (Math.random() * 2);
					direction = (ran == 0 ? 1 : 3);// 1 3
				} else if (x == 3 && y == 0) {
					direction = 1;
				} else if (x == 3 && y == 3) {
					direction = 3;
				}
				break;
			case 3:
				// 3: back(남)
				if (y != 3 && x != 0 && x != 3) { // 아래쪽을 제외하고 모든 방향으로 진행이 가능할 때
					ran = (int) (Math.random() * 3);// 0 1 2
					direction = ran;
					return;
				} else if (y == 3 && x != 0 && x != 3) { // 오른쪽과 왼쪽으로 진행이 가능할 때
					ran = (int) (Math.random() * 2);// 0 2
					direction = (ran == 0 ? 0 : 2);
				} else if (y != 3 && x == 0) { // 위쪽와 오른쪽으로 진행이 가능할 때
					ran = (int) (Math.random() * 2);// 0 2
					direction = (ran == 0 ? 0 : 2);
				} else if (y != 3 && x == 3) { // 위쪽와 왼쪽으로 진행이 가능할 때
					ran = (int) (Math.random() * 2);// 1,2
					direction = ran + 1;
				} else if (x == 0 && y == 3) {
					direction = 0;
				} else if (x == 3 && y == 3) {
					direction = 2;
				}
				break;
			}// end switch
			switch (direction) {
			case 0:
				nextx = x + 1;
				nexty = y;
				break;
			case 1:
				nextx = x;
				nexty = y + 1;
				break;
			case 2:
				nextx = x - 1;
				nexty = y;
				break;
			case 3:
				nextx = x;
				nexty = y - 1;
				break;
			}
		}

		// 진행 방향으로 wumpus가 존재한다면
		if (isWumpus) {
			// 화살이 있으면 wumpus가 있다고 생각하는 곳 중에 한 곳을 화살로 쏨
			// 화살이 없으면 다시 돌아감
			if (arrow > 0) {
				boolean pass = false;
				boolean kill = false;
				int newx = prevx, newy = prevy;
				while (!pass) {
					// 0:right,
					switch (direction) {
					case 0:
						if (x != 3) {
							if (!rooms[x + 1][y].visit) {
								arrow -= 1;
								for (int i = x + 1; i <= 3; i++) {
									rooms[i][y].wumpus = false;
									rooms[i][y].perceive.scream = true;
									
									kill = true;
									if (i + 1 == wumx && y + 1 == wumy) {
										offstench(wumx, wumy);
										System.out.println("kill Wumpus1");
									} else if (i + 1 == secwumx && y + 1 == secwumy) {
										offstench(secwumx, secwumy);
										System.out.println("kill Wumpus2");
									}
								}
								newx = x + 1;
								newy = y;
							}
						}
						break;
					case 1:
						// 1: forward,
						if (y != 3) {
							if (!rooms[x][y + 1].visit) {
								arrow -= 1;
								for (int i = y + 1; i <= 3; i++) {
									rooms[x][i].wumpus = false;
									rooms[x][i].perceive.scream = true;
									
									kill = true;
									if (x + 1 == wumx && i + 1 == wumy) {
										offstench(wumx, wumy);
										System.out.println("kill Wumpus1");
									} else if (x + 1 == secwumx && i + 1 == secwumy) {
										offstench(secwumx, secwumy);
										System.out.println("kill Wumpus2");
									}

								}
							}
							newx = x;
							newy = y + 1;
						}
						break;
					case 2:
						// 2: left
						if (x != 0) {
							if (!rooms[x - 1][y].visit) {
								arrow -= 1;
								for (int i = x - 1; i >= 0; i--) {
									rooms[i][y].wumpus = false;
									rooms[i][y].perceive.scream = true;
									
									kill = true;
									if (i + 1 == wumx && y + 1 == wumy) {
										offstench(wumx, wumy);
										System.out.println("kill Wumpus1");
									} else if (i + 1 == secwumx && y + 1 == secwumy) {
										offstench(secwumx, secwumy);
										System.out.println("kill Wumpus2");
									}
								}
								newx = x - 1;
								newy = y;
							}
						}
						break;
					case 3:
						// , 3: back
						if (y != 0) {
							if (!rooms[x][y - 1].visit) {
								arrow -= 1;
								for (int i = y - 1; i >= 0; i--) {
									rooms[x][i].wumpus = false;
									rooms[x][i].perceive.scream = true;
									
									kill = true;
									if (x + 1 == wumx && i + 1 == wumy) {
										offstench(wumx, wumy);
										System.out.println("kill Wumpus1");
									} else if (x + 1 == secwumx && i + 1 == secwumy) {
										offstench(secwumx, secwumy);
										System.out.println("kill Wumpus2");
									}

								}
								newx = x;
								newy = y - 1;
							}
						}
						break;
					}// end switch
					if (kill)
						pass = true;
				} // end while
				if (kill) {
					nextx = newx;
					nexty = newy;
					System.out.print("The Agent Killed the Wumpus at ("); //$
					System.out.print(x);
					System.out.print(", ");
					System.out.print(y);
					System.out.println(")");
					return;
				} else {
					nextx = prevx;
					nexty = prevy;
					return;
				}
			} else {
				nextx = prevx;
				nexty = prevy;
				return;
			}
		}
	}// end of findnext

	//agent가 gold를 인식했을 때 주변을 탐색한다.
	public static boolean findgold(int currentx, int currenty) {
		boolean success = false;
		for (int i = 0; i < 4; i++) {
			switch (i) {
			case 0:
				if (currentx < 3 && rooms[currentx + 1][currenty].gold) {
					success = true;
					System.out.print("GOLD : (" + (currentx + 2) + " ," + (currenty + 1) + ")\n");
					pathx[pathindex] = currentx + 1;
					pathy[pathindex++] = currenty;
					break;
				}
				break;
			case 1:
				if (currenty < 3 && rooms[currentx][currenty + 1].gold) {
					success = true;
					System.out.print("GOLD : (" + (currentx + 1) + " ," + (currenty + 2) + ")\n");
					pathx[pathindex] = currentx;
					pathy[pathindex++] = currenty + 1;
					break;
				}
				break;
			case 2:
				if (currentx > 0 && rooms[currentx - 1][currenty].gold) {
					success = true;
					System.out.print("GOLD : (" + (currentx) + " ," + (currenty + 1) + ")\n");
					pathx[pathindex] = currentx - 1;
					pathy[pathindex++] = currenty;
					break;
				}
				break;
			case 3:
				if (currenty > 0 && rooms[currentx][currenty - 1].gold) {
					success = true;
					System.out.print("GOLD : (" + (currentx + 1) + " ," + (currenty) + ")\n");
					pathx[pathindex] = currentx;
					pathy[pathindex++] = currenty - 1;
					break;
				}
				break;
			}
		}
		return success;
	} //end of findgold
	
	public static void init_toRestart(int currentx, int currenty) {
		arrow = 3;
		// 방문 기록 초기화
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				rooms[i][j].visit = false;
			}
		}
		// 저장한 경로 초기화
		prevx = currentx;
		prevy = currenty;
		for (int i = 0; i < pathindex; i++) {
			pathx[i] = -1;
			pathy[i] = -1;
		}
		pathindex = 0;
		direction = 0;
	}
	//gold 위치 맵에서 초기화
	public static void locategold() {
		int a = 1, b = 1;
		while ((a == 1 && b == 1) || (a == 3 && b == 1) || (a == 2 && b == 2) || (a == 1 && b == 3)
				|| (a == 1 && b == 2) || (a == 2 && b == 1)) {
			a = (int) (Math.random() * 4 + 1); // 1~4까지의 수 중에서 랜덤하게 생성, x좌표가 됨
			b = (int) (Math.random() * 4 + 1);
		}

		rooms[a - 1][b - 1].gold = true;
		goldx = a;
		goldy = b; // gold의 x,y좌표가 저장됨.
	} //end of locategold

	//wumpus 위치 맵에서 초기화
	public static void locatewupus() {

		int a = 1;
		int b = 1;
		while (a == 1 && b == 1) {
			a = (int) (Math.random() * 4 + 1); // 1~4까지의 수 중에서 랜덤하게 생성, x좌표가 됨
			b = (int) (Math.random() * 4 + 1); // y좌표가 됨
		}
		while (a == goldx && b == goldy) {
			a = (int) (Math.random() * 4 + 1); // 1~4까지의 수 중에서 랜덤하게 생성, x좌표가 됨
			b = (int) (Math.random() * 4 + 1); // y좌표가 됨
		}
		while ((a == 1 && b == 1) || (a == 3 && b == 1) || (a == 2 && b == 2) || (a == 1 && b == 3)
				|| (a == 1 && b == 2) || (a == 2 && b == 1)) {
			a = (int) (Math.random() * 4 + 1); // 1~4까지의 수 중에서 랜덤하게 생성, x좌표가 됨
			b = (int) (Math.random() * 4 + 1);
		}
		rooms[a - 1][b - 1].wumpus = true;
		wumx = a;
		wumy = b; // wumpus의 x,y좌표가 저장됨

//웜퍼스 위치 출력
		System.out.print("Location of Wumpus1 : (");
		System.out.println(a + ", " + b + ")");

		a = 1;
		b = 1;
		while (a == 1 && b == 1) {
			a = (int) (Math.random() * 4 + 1); // 1~4까지의 수 중에서 랜덤하게 생성, x좌표가 됨
			b = (int) (Math.random() * 4 + 1); // y좌표가 됨
		}
		while (a == goldx && b == goldy) {
			a = (int) (Math.random() * 4 + 1); // 1~4까지의 수 중에서 랜덤하게 생성, x좌표가 됨
			b = (int) (Math.random() * 4 + 1); // y좌표가 됨
		}
		while (a == wumx && b == wumy) {
			a = (int) (Math.random() * 4 + 1); // 1~4까지의 수 중에서 랜덤하게 생성, x좌표가 됨
			b = (int) (Math.random() * 4 + 1);
		}
		while ((a == 1 && b == 1) || (a == 3 && b == 1) || (a == 2 && b == 2) || (a == 1 && b == 3)
				|| (a == 1 && b == 2) || (a == 2 && b == 1)) {
			a = (int) (Math.random() * 4 + 1); // 1~4까지의 수 중에서 랜덤하게 생성, x좌표가 됨
			b = (int) (Math.random() * 4 + 1);
		}
		rooms[a - 1][b - 1].wumpus = true;
		secwumx = a;
		secwumy = b; // wumpus의 x,y좌표가 저장됨

//웜퍼스2 위치 출력
		System.out.print("Location of Wumpus2 : (");
		System.out.println(a + ", " + b + ")");

	} // end of locatewupus

	// pit의 위치 맵에서 초기화
	public static void locatepits() {
		int m = 1, n = 1;
		for (int i = 0; i < 2; i++) {
			m = 1;
			n = 1;
			while (m == 1 && n == 1) {
				m = (int) (Math.random() * 4 + 1);
				n = (int) (Math.random() * 4 + 1);
				while ((m == 3 && n == 1) || (m == 2 && n == 2) || (m == 1 && n == 3) || (m == 1 && n == 2)
						|| (m == 2 && n == 1)) {
					m = (int) (Math.random() * 4 + 1);
					n = (int) (Math.random() * 4 + 1);
				}
				if ((m != 1 || n != 1) && (rooms[m - 1][n - 1].gold == false) && (rooms[m - 1][n - 1].wumpus == false)
						&& (rooms[m - 1][n - 1].pit == false)) {
					rooms[m - 1][n - 1].pit = true;
					break;
				} else {
					m = 1;
					n = 1;
				}
			}
		}
	}// end of locatepits

	public static void deletepits() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				rooms[i][j].pit = false;
			}
		}
	}// end of deletepits

	public static void setglitter(int x, int y) {
		if ((x - 1) != 0) {
			rooms[x - 2][y - 1].perceive.glitter = true;
		}
		if ((x + 1) != 5) {
			rooms[x][y - 1].perceive.glitter = true;
		}
		if ((y - 1) != 0) {
			rooms[x - 1][y - 2].perceive.glitter = true;
		}
		if ((y + 1) != 5) {
			rooms[x - 1][y].perceive.glitter = true;
		}
	}// end of glitter

	public static void setstench(int x, int y) {
		if ((x - 1) != 0) {
			rooms[x - 2][y - 1].perceive.stench = true;
		}
		if ((x + 1) != 5) {
			rooms[x][y - 1].perceive.stench = true;
		}
		if ((y - 1) != 0) {
			rooms[x - 1][y - 2].perceive.stench = true;
		}
		if ((y + 1) != 5) {
			rooms[x - 1][y].perceive.stench = true;
		}
	}// end of setstench

	public static void offstench(int x, int y) {
		if ((x - 1) != 0) {
			rooms[x - 2][y - 1].perceive.stench = false;
		}
		if ((x + 1) != 5) {
			rooms[x][y - 1].perceive.stench = false;
		}
		if ((y - 1) != 0) {
			rooms[x - 1][y - 2].perceive.stench = false;
		}
		if ((y + 1) != 5) {
			rooms[x - 1][y].perceive.stench = false;
		}
	}// end of offstench

	public static void setbreeze() {
		for (int x = 1; x < 5; x++) {
			for (int y = 1; y < 5; y++) {
				if (rooms[x - 1][y - 1].pit) {
					if ((x - 1) != 0) {
						rooms[x - 2][y - 1].perceive.breeze = true;
					}
					if ((x + 1) != 5) {
						rooms[x][y - 1].perceive.breeze = true;
					}
					if ((y - 1) != 0) {
						rooms[x - 1][y - 2].perceive.breeze = true;
					}
					if ((y + 1) != 5) {
						rooms[x - 1][y].perceive.breeze = true;
					}
				}
			}
		}
	}// end of setbreeze

	public static void outputstate() {
		PrintWriter output = null;

		try {
			output = new PrintWriter(new FileOutputStream("state.txt"));
		} catch (FileNotFoundException e) {
			System.out.println("Error");
			System.exit(0);
		}
		// output.println(" =========================================");
		int pitnum = 0;
		for (int i = 3; i >= 0; i--) {

			output.print(" ");
			output.print(i + 1);

			output.print("|");
			for (int j = 0; j < 4; j++) {
				// output.println("-----------------------------------------");
				if (rooms[j][i].gold || rooms[j][i].wumpus) {
					if (rooms[j][i].gold)
						output.print("G|");
					if (rooms[j][i].wumpus)
						output.print("W|");
				}
				if (rooms[j][i].pit) {
					output.print("P|");
					pitnum++;
				}
				if (rooms[j][i].gold == false && rooms[j][i].wumpus == false && rooms[j][i].pit == false) {
					output.print(" |");

				}
			}
			output.println();
			// output.println(" =========================================");
		}
		output.println("   1 2 3 4 ");
		output.close();
		System.out.println("Number of Pits : " + pitnum);
	}// end of outputstate

	public static void outputpercept() {
		PrintWriter output = null;
		try {
			output = new PrintWriter(new FileOutputStream("percept.txt"));
		} catch (FileNotFoundException e) {
			System.out.println("Error");
			System.exit(0);
		}
		for (int i = 3; i >= 0; i--) {
			if (i > 8) {
				output.print(i + 1);
			} else {
				output.print(" ");
				output.print(i + 1);
			}
			output.print("|");
			for (int j = 0; j < 4; j++) {
				if (rooms[j][i].perceive.stench) {
					output.print("S|");
				}
				if (!rooms[j][i].perceive.stench) {
					output.print(" |");
				}
			}
			if (i > 8) {
				output.print(" ");
				output.print(i + 1);
			} else {
				output.print("  ");
				output.print(i + 1);
			}
			output.print("|");
			for (int j = 0; j < 4; j++) {
				if (rooms[j][i].perceive.breeze) {
					output.print("B|");
				} else {
					output.print(" |");
				}
			}
			if (i > 8) {
				output.print(" ");
				output.print(i + 1);
			} else {
				output.print("  ");
				output.print(i + 1);
			}
			output.print("|");
			for (int j = 0; j < 4; j++) {
				if (rooms[j][i].perceive.glitter) {
					output.print("G|");
				} else {
					output.print(" |");
				}
			}
			output.println();
		}
		output.print("   1 2 3 4 ");
		output.print("   1 2 3 4 ");
		output.println("    1 2 3 4 ");
		output.close();
	}// end of outputpercept

	public static void outputpath(int result, int cause) {
		PrintWriter output = null;
		try {
			output = new PrintWriter(new FileOutputStream("path.txt"));
		} catch (FileNotFoundException e) {
			System.out.println("Error");
			System.exit(0);
		}
		//쓰기
		output.println("*** Path of Agent ***");
		
		for (int i = 0; i < pathindex; i++) {
			int x = pathx[i];
			int y = pathy[i];
			output.print("(");
			output.print(x + 1);
			output.print(", ");
			output.print(y + 1);
			output.println(")");
		}
		
		if (result == 1) {
			for (int i = pathindex - 2; i >= 0; i--) {
				int x = pathx[i];
				int y = pathy[i];
				output.print("(");
				output.print(x + 1);
				output.print(", ");
				output.print(y + 1);
				output.println(")");
			}
			System.out.println("Path Length : " + (pathindex + pathindex-1));
			System.out.println("SUCCESS : Climb");
			output.println("SUCCESS : Climb");
			output.println("SUCCESS : The Agent Grabbed the GOLD!!!!!");
		} else {
			System.out.println("Path Length : " + (pathindex));
			if (cause == 1)
				output.println("FAIL : The Agent Died because of Pit");
			else
				output.println("FAIL : The Agent Died because of Wumpus");
		}
		output.close();
	}// end of outputpath
}// end of class