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

		//1. wumpus world�� ȯ�� ���� �ʱ�ȭ

		//������� ������ �濡 pits, wumpus, gold�� ����
		//wumpus, gold, pits�� random�ϰ� �����Ѵ�.
		//��, wumpus�� gold�� pits�� ���� �濡 �������� �ʴ´�.
		//������� pits�� �ѷ��ο�  ���� ���� �ʱ�ȭ�� �ٽ� �Ѵ�. 

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				rooms[i][j] = new Room();
				rooms[i][j].x = i + 1;
				rooms[i][j].y = j + 1;
			}
		}
		//�ϳ��� gold��  ��ġ�� random�ϰ� ��´�.
		locategold();
		//4x4=16�� ���� 15%�� 2.4�� �̹Ƿ� 2���� wumpus�� �����Ѵ�.
		locatewupus();

		//4x4=16�� ���� 15%�� 2.4�� �̹Ƿ� 2���� pits�� �����Ѵ�.
		//�̹� wumpus�� gold�� pits�� ���� �ٸ� ��ġ�� �ִ�. 
		locatepits();

		//�ʱ�ȭ�� �������� pits,wumpus,gold�� ���� �� ���� breeze,stench,glitter�� �����Ѵ�.
		// gold�� �ִ� ���� glitter ����
		setglitter(goldx, goldy);
		//wumpus�� ���� �� ���� stench ����
		setstench(wumx, wumy);
		setstench(secwumx, secwumy);
		//pit�� ���� �� ���� breeze ����
		setbreeze();

		//text ���Ͽ� �ʱ�ȭ�� ���� ���
		outputstate(); // pit, gold, wumpus�� ��ġ ���
		outputpercept(); // breeze, glitter, stench ���� ���
		//���⼭����
		//2. gold�� ã�� ���� �̵�

		//gold�� ã���� �����Ѵ�.
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
					ai.infer[currentx][currenty].p = true; // wumpus�� ���ؼ� agent�� �׾��ٸ�, ���� ��ġ�� wumpus�� ������ ����Ѵ�.
					System.out.println("FAIL : The Agent Died because of Pit");
				} else if (failcause == 2) {
					outputpath(2, 2);
					ai.infer[currentx][currenty].w = true; // pit�� ���ؼ� agent�� �׾��ٸ�, ���� ��ġ�� pit�� ������ ����Ѵ�.
					System.out.println("FAIL : The Agent Died because of Wumpus");
				}
				// break -> continue : ���� �ÿ��� �ٽ� ó�� ��ġ���� �����
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

			//���� ��ġ�� �̵�... 
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
		// ���� �������� pit �Ǵ� wumpus�� �ִ��� Ȯ���Ѵ�.
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
		if ((!rooms[x][y].perceive.breeze) && (!rooms[x][y].perceive.stench)) // breeze�� stench�� �ȴ�������.
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
					// right(��)
					if (x == 3) {
						if (y != 3 && ran == 1)
							direction = 1; // �� ���� ���� ������
						else if (y != 0 && ran == 0)
							direction = 3; // �� �Ʒ��� ���� ������
						else if (y == 3)
							direction = 3; // �� ���� �ִٸ�
						else if (y == 0)
							direction = 1; // �� �Ʒ��� �ִٸ�
					}
					break;
				case 1:
					// 1: forward(��)
					if (y == 3) {
						if (x != 3 && ran == 1)
							direction = 0; // �� �����ʿ� ���� ������
						else if (x != 0 && ran == 0)
							direction = 2; // �� ���ʿ� ���� ������
						else if (x == 3)
							direction = 2; // �� �����ʿ� �ִٸ�
						else if (x == 0)
							direction = 0; // �� ���ʿ� �ִٸ�
					}
					break;
				case 2:
					// 2: left(��)
					if (x == 0) {
						if (y != 3 && ran == 1)
							direction = 1; // �� ���� ���� ������
						else if (y != 0 && ran == 0)
							direction = 3; // �� �Ʒ��� ���� ������
						else if (y == 3)
							direction = 3; // �� ���� �ִٸ�
						else if (y == 0)
							direction = 1; // �� �Ʒ��� �ִٸ�
					}
					break;
				case 3:
					// 3: back(��)
					if (y == 0) {
						if (x != 3 && ran == 1)
							direction = 0; // �� �����ʿ� ���� ������
						else if (x != 0 && ran == 0)
							direction = 2; // �� ���ʿ� ���� ������
						else if (x == 3)
							direction = 2; // �� �����ʿ� �ִٸ�
						else if (x == 0)
							direction = 0; // �� ���ʿ� �ִٸ�
					}
					break;
				}// end switch
			}
		}
		// ������ �� ��쳪 �ٶ��� ������ ��� -> ����������� ����
		else if ((rooms[x][y].perceive.breeze) || (rooms[x][y].perceive.stench)) {
			int pre_direction = direction;
			// ���� ���� ���� �ִٸ�
			if (x == 0 || x == 3 || y == 0 || y == 3) {
				int ran = (int) (Math.random() * 2);
				switch (direction) {
				case 0:
					// right(��)
					if (x == 3) {
						if (y != 3 && ran == 1)
							direction = 1; // �� ���� ���� ������
						else if (y != 0 && ran == 0)
							direction = 3; // �� �Ʒ��� ���� ������
						else if (y == 3)
							direction = 3; // �� ���� �ִٸ�
						else if (y == 0)
							direction = 1; // �� �Ʒ��� �ִٸ�
					}
					break;
				case 1:
					// 1: forward(��)
					if (y == 3) {
						if (x != 3 && ran == 1)
							direction = 0; // �� �����ʿ� ���� ������
						else if (x != 0 && ran == 0)
							direction = 2; // �� ���ʿ� ���� ������
						else if (x == 3)
							direction = 2; // �� �����ʿ� �ִٸ�
						else if (x == 0)
							direction = 0; // �� ���ʿ� �ִٸ�
					}
					break;
				case 2:
					// 2: left(��)
					if (x == 0) {
						if (y != 3 && ran == 1)
							direction = 1; // �� ���� ���� ������
						else if (y != 0 && ran == 0)
							direction = 3; // �� �Ʒ��� ���� ������
						else if (y == 3)
							direction = 3; // �� ���� �ִٸ�
						else if (y == 0)
							direction = 1; // �� �Ʒ��� �ִٸ�
					}
					break;
				case 3:
					// 3: back(��)
					if (y == 0) {
						if (x != 3 && ran == 1)
							direction = 0; // �� �����ʿ� ���� ������
						else if (x != 0 && ran == 0)
							direction = 2; // �� ���ʿ� ���� ������
						else if (x == 3)
							direction = 2; // �� �����ʿ� �ִٸ�
						else if (x == 0)
							direction = 0; // �� ���ʿ� �ִٸ�
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

		// ���� �������� pit�� �����Ѵٸ�
		if (isPit) {
			int ran;
			switch (direction) {
			case 0:
				// right(��)
				if (y != 3 && y != 0 && x != 0) { // �������� �����ϰ� ��� �������� ������ ������ ��
					ran = (int) (Math.random() * 3);
					direction = ran + 1;
					return;
				} else if (y == 3 && x != 0) { // �Ʒ��� �������� ������ ������ ��
					ran = (int) (Math.random() * 2);
					direction = ran + 2;
				} else if (y == 0 && x != 0) { // ���� �������� ������ ������ ��
					ran = (int) (Math.random() * 2);
					direction = ran + 1;
				} else if (y != 3 && y != 0 && x == 0) { // ���� �Ʒ��� ������ ������ ��
					ran = (int) (Math.random() * 2);
					direction = (ran == 0 ? 1 : 3);
				} else if (y == 3 && x == 0) { // �Ʒ������θ� �� �� ���� ��
					direction = 3;
				}

				break;
			case 1:
				// 1: forward(��)
				if (y != 0 && x != 0 && x != 3) { // ������ �����ϰ� ��� �������� ������ ������ ��
					ran = (int) (Math.random() * 3);// 0 2 3
					direction = (ran == 2 ? 0 : ran + 2);
					return;
				} else if (y == 0 && x != 0 && x != 3) { // �����ʰ� �������� ������ ������ ��
					ran = (int) (Math.random() * 2);// 0 2
					direction = (ran == 0 ? 0 : 2);
				} else if (y != 0 && x == 0) { // �Ʒ��ʿ� ���������� ������ ������ ��
					ran = (int) (Math.random() * 2);// 0 3
					direction = (ran == 0 ? 0 : 3);
				} else if (y != 0 && x == 3) { // �Ʒ��ʿ� �������� ������ ������ ��
					ran = (int) (Math.random() * 2);// 2,3
					direction = ran + 2;
				} else if (x == 3 && y == 0) {
					direction = 2;
				}
				break;
			case 2:
				// 2: left(��)
				if (y != 3 && y != 0 && x != 3) { // ������ �����ϰ� ��� �������� ������ ������ ��
					ran = (int) (Math.random() * 3); // 0 1 3
					direction = (ran == 2 ? 3 : ran);
					return;
				} else if (y == 3 && x != 3) { // �Ʒ��ʿ� ���������� ������ ������ ��
					ran = (int) (Math.random() * 2);
					direction = (ran == 0 ? 0 : 3); // 0 3
				} else if (y == 0 && x != 3) { // ���� ���������� ������ ������ ��
					ran = (int) (Math.random() * 2);
					direction = ran; // 0 1
				} else if (y != 3 && y != 0 && x == 3) { // ���� �Ʒ��� ������ ������ ��
					ran = (int) (Math.random() * 2);
					direction = (ran == 0 ? 1 : 3);// 1 3
				} else if (x == 3 && y == 0) {
					direction = 1;
				} else if (x == 3 && y == 3) {
					direction = 3;
				}
				break;
			case 3:
				// 3: back(��)
				if (y != 3 && x != 0 && x != 3) { // �Ʒ����� �����ϰ� ��� �������� ������ ������ ��
					ran = (int) (Math.random() * 3);// 0 1 2
					direction = ran;
					return;
				} else if (y == 3 && x != 0 && x != 3) { // �����ʰ� �������� ������ ������ ��
					ran = (int) (Math.random() * 2);// 0 2
					direction = (ran == 0 ? 0 : 2);
				} else if (y != 3 && x == 0) { // ���ʿ� ���������� ������ ������ ��
					ran = (int) (Math.random() * 2);// 0 2
					direction = (ran == 0 ? 0 : 2);
				} else if (y != 3 && x == 3) { // ���ʿ� �������� ������ ������ ��
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

		// ���� �������� wumpus�� �����Ѵٸ�
		if (isWumpus) {
			// ȭ���� ������ wumpus�� �ִٰ� �����ϴ� �� �߿� �� ���� ȭ��� ��
			// ȭ���� ������ �ٽ� ���ư�
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

	//agent�� gold�� �ν����� �� �ֺ��� Ž���Ѵ�.
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
		// �湮 ��� �ʱ�ȭ
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				rooms[i][j].visit = false;
			}
		}
		// ������ ��� �ʱ�ȭ
		prevx = currentx;
		prevy = currenty;
		for (int i = 0; i < pathindex; i++) {
			pathx[i] = -1;
			pathy[i] = -1;
		}
		pathindex = 0;
		direction = 0;
	}
	//gold ��ġ �ʿ��� �ʱ�ȭ
	public static void locategold() {
		int a = 1, b = 1;
		while ((a == 1 && b == 1) || (a == 3 && b == 1) || (a == 2 && b == 2) || (a == 1 && b == 3)
				|| (a == 1 && b == 2) || (a == 2 && b == 1)) {
			a = (int) (Math.random() * 4 + 1); // 1~4������ �� �߿��� �����ϰ� ����, x��ǥ�� ��
			b = (int) (Math.random() * 4 + 1);
		}

		rooms[a - 1][b - 1].gold = true;
		goldx = a;
		goldy = b; // gold�� x,y��ǥ�� �����.
	} //end of locategold

	//wumpus ��ġ �ʿ��� �ʱ�ȭ
	public static void locatewupus() {

		int a = 1;
		int b = 1;
		while (a == 1 && b == 1) {
			a = (int) (Math.random() * 4 + 1); // 1~4������ �� �߿��� �����ϰ� ����, x��ǥ�� ��
			b = (int) (Math.random() * 4 + 1); // y��ǥ�� ��
		}
		while (a == goldx && b == goldy) {
			a = (int) (Math.random() * 4 + 1); // 1~4������ �� �߿��� �����ϰ� ����, x��ǥ�� ��
			b = (int) (Math.random() * 4 + 1); // y��ǥ�� ��
		}
		while ((a == 1 && b == 1) || (a == 3 && b == 1) || (a == 2 && b == 2) || (a == 1 && b == 3)
				|| (a == 1 && b == 2) || (a == 2 && b == 1)) {
			a = (int) (Math.random() * 4 + 1); // 1~4������ �� �߿��� �����ϰ� ����, x��ǥ�� ��
			b = (int) (Math.random() * 4 + 1);
		}
		rooms[a - 1][b - 1].wumpus = true;
		wumx = a;
		wumy = b; // wumpus�� x,y��ǥ�� �����

//���۽� ��ġ ���
		System.out.print("Location of Wumpus1 : (");
		System.out.println(a + ", " + b + ")");

		a = 1;
		b = 1;
		while (a == 1 && b == 1) {
			a = (int) (Math.random() * 4 + 1); // 1~4������ �� �߿��� �����ϰ� ����, x��ǥ�� ��
			b = (int) (Math.random() * 4 + 1); // y��ǥ�� ��
		}
		while (a == goldx && b == goldy) {
			a = (int) (Math.random() * 4 + 1); // 1~4������ �� �߿��� �����ϰ� ����, x��ǥ�� ��
			b = (int) (Math.random() * 4 + 1); // y��ǥ�� ��
		}
		while (a == wumx && b == wumy) {
			a = (int) (Math.random() * 4 + 1); // 1~4������ �� �߿��� �����ϰ� ����, x��ǥ�� ��
			b = (int) (Math.random() * 4 + 1);
		}
		while ((a == 1 && b == 1) || (a == 3 && b == 1) || (a == 2 && b == 2) || (a == 1 && b == 3)
				|| (a == 1 && b == 2) || (a == 2 && b == 1)) {
			a = (int) (Math.random() * 4 + 1); // 1~4������ �� �߿��� �����ϰ� ����, x��ǥ�� ��
			b = (int) (Math.random() * 4 + 1);
		}
		rooms[a - 1][b - 1].wumpus = true;
		secwumx = a;
		secwumy = b; // wumpus�� x,y��ǥ�� �����

//���۽�2 ��ġ ���
		System.out.print("Location of Wumpus2 : (");
		System.out.println(a + ", " + b + ")");

	} // end of locatewupus

	// pit�� ��ġ �ʿ��� �ʱ�ȭ
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
		//����
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