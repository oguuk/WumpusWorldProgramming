
public class Room {
	public int x;	//방의 x좌표
	public int y;	//방의 y좌표
	public boolean pit;		//방에 pit가 있을 때 설정
	public boolean wumpus;	//방에 wumpus가 있을 때 설정
	public boolean gold;	//방에 gold가 있을 때 설정
	public boolean visit;	//agent가 방문하면 설정
	public Percept perceive;	//방문시 인식되는 상태
	public Room()
	{
		x = 0;
		y = 0;
		pit = false;
		wumpus = false;
		gold = false;
		perceive = new Percept();
		visit = false;
	}
}
