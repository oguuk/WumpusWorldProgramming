package wumpusWorld;

public class Room {
	public int x;	//���� x��ǥ
	public int y;	//���� y��ǥ
	public boolean pit;		//�濡 pit�� ���� �� ����
	public boolean wumpus;	//�濡 wumpus�� ���� �� ����
	public boolean gold;	//�濡 gold�� ���� �� ����
	public boolean visit;	//agent�� �湮�ϸ� ����
	public Percept perceive;	//�湮�� �νĵǴ� ����
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

