import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class WumpusWorld {

	public static Room[][] rooms = new Room[20][20];	
	//public static ArrayList<Integer> pathx = new ArrayList<Integer>(1000);
	//public static ArrayList<Integer> pathy = new ArrayList<Integer>(1000);
	public static int[] pathx = new int[10000];
	public static int[] pathy = new int[10000];	
	public static int pathindex = 0;
	public static int nextx, nexty;
	public static int prevx, prevy;
	public static Agent ai = new Agent();
	public static boolean arrow = true;
	
	public static void main(String[] args) {
		
		//1. wumpus world�� ȯ�� ���� �ʱ�ȭ
		
		//������� ������ �濡 pits, wumpus, gold�� ����
		//wumpus, gold, pits�� random�ϰ� �����Ѵ�.
		//��, wumpus�� gold�� pits�� ���� �濡 �������� �ʴ´�.
		//������� pits�� �ѷ��ο�  ���� ���� �ʱ�ȭ�� �ٽ� �Ѵ�.
		//���� gold�� pits�� �ѷ��ο� ���� �ÿ��� �ʱ�ȭ�� �ٽ� �� �ش�.			
		for(int i=0;i<20;i++)
		{
			for(int j=0;j<20;j++)
			{
				rooms[i][j] = new Room();
				rooms[i][j].x = i+1;
				rooms[i][j].y = j+1;
			}
		}
		
		//���� �ϳ���  wumpus�� �ϳ��� gold��  ��ġ�� random�ϰ� ��´�.
		//��, start(1,1)�ϴ� �濡 ��ġ�ؼ��� �ȵȴ�.		
		int a = 1, b = 1;
		while(a==1 && b==1)
		{
			a = (int)(Math.random()*20+1);	//1~20������ �� �߿��� �����ϰ� ����, x��ǥ�� ��
			b = (int)(Math.random()*20+1);	//y��ǥ�� ��			
		}
		rooms[a-1][b-1].gold = true;
		int goldx = a, goldy = b;		//gold�� x,y��ǥ�� �����.
		a = 1; b = 1;
		while(a==1 && b==1)
		{
			a = (int)(Math.random()*20+1);	//1~20������ �� �߿��� �����ϰ� ����, x��ǥ�� ��
			b = (int)(Math.random()*20+1);	//y��ǥ�� ��	
		}
		rooms[a-1][b-1].wumpus = true;
		int wumx = a, wumy = b;		//wumpus�� x,y��ǥ�� �����
		
		//20x20=400�� ���� 20%�� 80�̹Ƿ� 80���� pits�� �����Ѵ�.
		//�̹� wumpus�� gold�� �ִ� �濡 pits�� �������� �ʴ´�.
		locatepits();
		//gold�� pits�� �ѷ��ο� �ְų� ��������� pits�� �ѷ��ο� �ִ� ��
		//��������� gold�� �̸��� ��ΰ� �������� ������ pits�� �ٽ� �����ؾ� �Ѵ�.		
/*		boolean exist = false;
		while(!exist)
		{
			exist = isExist();
			if(exist)
			{
				break;
			}
			else
			{
				deletepits();
				locatepits();
			}
		}
*/				
		//�ʱ�ȭ�� �������� pits,wumpus,gold�� ���� �� ���� breeze,stench,glitter�� �����Ѵ�.
		//���� �� ���� wall bump�� �����Ѵ�.
		//gold�� �ִ� ���� glitter ����
		rooms[goldx-1][goldy-1].perceive.glitter = true;		               
		//wumpus�� ���� �� ���� stench ����
		setstench(wumx,wumy);
		//pit�� ���� �� ���� breeze ����
		setbreeze();
								
		//���۽� ��ġ ���
		System.out.print("Location of Wumpus : (");
		System.out.println(a+", "+b+")");	
		
		//text ���Ͽ� �ʱ�ȭ�� ���� ���
		outputstate();	//pit, gold, wumpus�� ��ġ ���
		outputpercept();	//breeze, glitter, stench ���� ���

		//2. gold�� ã�� ���� �̵�
		
		//�� �� ���¸� �ν��Ͽ� pits�� wumpus�� ���ٰ� �����Ǵ� ������ �̵��Ѵ�.
		//ok�� �濡�� �̵��� ���� �������̸� random�ϰ� �� ������ �̵��Ѵ�.
		//pits�� wumpus�� �ִٰ� �����Ǹ� �ٽ� backtracking�Ͽ� ���ٰ� �����Ǵ� ������ �̵��Ѵ�.
		//���� pits�� wumpus�� �������� �𸣴� ���� �������� �ϴ� ��쿡��,
		//����, wumpus�� ���� ���̶�� �����Ǵ� ���� arrow�� ���.
		//pits�� ���� ���̶�� ����Ǵ� ���, ���� ��ġ���� �� �� �ִ� ��θ� random�ϰ� �� ���� ����.
		//gold�� ã���� �����Ѵ�.
		boolean success = false;
		boolean fail = false;
		int currentx=0, currenty=0;
		int failcause = 0; 				
		//int pathindex = 0;
		prevx = currentx;
		prevy = currenty;
		for(int i=0;i<10000;i++)
		{
			pathx[i] = -1;
			pathy[i] = -1;
		}
		while(!success)
		{
			//pathx.add(pathindex, currentx);
			//pathy.add(pathindex++, currenty);
			pathx[pathindex] = currentx;
			pathy[pathindex++] = currenty;
			rooms[currentx][currenty].visit = true;
						
			if(rooms[currentx][currenty].perceive.glitter)
			{
				success = true;
			}
			if(rooms[currentx][currenty].pit)
			{
				failcause = 1;
				fail = true;
			}
			else if(rooms[currentx][currenty].wumpus)
			{
				failcause = 2;
				fail = true;
			}
			
			if(success)
			{
				outputpath(1,0);
				System.out.println("SUCCESS : The Agent Grabbed the GOLD!!!!!");
				break;
			}
			if(fail)
			{
				if(failcause==1)
				{
					outputpath(2,1);
					System.out.println("FAIL : The Agent Died because of Pit");
				}
				else if(failcause == 2)
				{
					outputpath(2,2);
					System.out.println("FAIL : The Agent Died because of Wumpus");
				}			
				break;
			}
			
			if((!rooms[currentx][currenty].perceive.breeze)&&(!rooms[currentx][currenty].perceive.stench))
			{
				setok(currentx,currenty);
			}
			else if(!rooms[currentx][currenty].perceive.breeze)
			{
				setpit(currentx,currenty);
			}
			else if(!rooms[currentx][currenty].perceive.stench)
			{
				setwum(currentx,currenty);
			}
			
			//���� ��ġ�� �̵�...			
			findnext(currentx,currenty);	
			prevx = currentx;
			prevy = currenty;
			currentx = nextx;
			currenty = nexty;
			
		}//end of while*/
	}//end of main
	
	public static void setok(int x, int y)
	{
		ai.infer[x][y].ok = true;
		if(x!=0)
			ai.infer[x-1][y].ok = true;
		if(x!=19)
			ai.infer[x+1][y].ok = true;
		if(y!=0)
			ai.infer[x][y-1].ok = true;
		if(y!=19)
			ai.infer[x][y+1].ok = true;		
	}//end of setok
	public static void setpit(int x, int y)
	{
		ai.infer[x][y].ok = true;
		if(x!=0)
		{
			if(!rooms[x-1][y].visit)
					ai.infer[x-1][y].pp = true;
		}
		if(x!=19)
		{
			if(!rooms[x+1][y].visit)
					ai.infer[x+1][y].pp = true;
		}	
		if(y!=0)
		{
			if(!rooms[x][y-1].visit)
					ai.infer[x][y-1].pp = true;
		}	
		if(y!=19)
		{
			if(!rooms[x][y+1].visit)
					ai.infer[x][y+1].pp = true;
		}		
	}//end of setpit
	public static void setwum(int x, int y)
	{
		ai.infer[x][y].ok = true;
		if(x!=0)
		{
			if(!rooms[x-1][y].visit)
					ai.infer[x-1][y].wp = true;
		}
		if(x!=19)
		{
			if(!rooms[x+1][y].visit)
					ai.infer[x+1][y].wp = true;
		}	
		if(y!=0)
		{
			if(!rooms[x][y-1].visit)
					ai.infer[x][y-1].wp = true;
		}	
		if(y!=19)
		{
			if(!rooms[x][y+1].visit)
					ai.infer[x][y+1].wp = true;
		}		
	}//end of setwum
	public static void findnext(int x, int y)
	{
		if((!rooms[x][y].perceive.breeze)&&(!rooms[x][y].perceive.stench))
		{
			boolean pass = false;			
			while(!pass)
			{
				int sel = (int)(Math.random()*4);				
				switch(sel)
				{
				case 0:	
					if(x!=0)
					{												
						if(!rooms[x-1][y].visit)
						{
							nextx = x-1;
							nexty = y;
							pass = true;							
						}
					}				
					break;
				case 1: 
					if(x!=19)
					{		
						if(!rooms[x+1][y].visit)
						{
							nextx = x+1;
							nexty = y;
							pass = true;							
						}
					}
					break;
				case 2: 
					if(y!=0)
					{					
						if(!rooms[x][y-1].visit)
						{
							nextx = x;
							nexty = y-1;
							pass = true;
						}
					}
					break;
				case 3: 
					if(y!=19)
					{					
						if(!rooms[x][y+1].visit)
						{
							nextx = x;
							nexty = y+1;
							pass = true;
						}
					}
					break;					
				}//end switch		
				int next = 0;
				if(x!=0)
				{
					if(!rooms[x-1][y].visit)
						next++;
				}
				if(x!=19)
				{							
					if(!rooms[x+1][y].visit)
						next++;
				}
				if(y!=0)
				{										
					if(!rooms[x][y-1].visit)
						next++;	
				}
				if(y!=19)
				{	
					if(!rooms[x][y+1].visit)
						next++;
				}
				if(next == 0)
				{
					sel = (int)(Math.random()*4);
					switch(sel)
					{
					case 0:	
						if(x!=0)
						{						
							if((x-1)!=prevx)							
							{
								nextx = x-1;
								nexty = y;
								pass = true;							
							}
						}				
						break;
					case 1: 
						if(x!=19)
						{							
							if((x+1)!=prevx)
							{
								nextx = x+1;
								nexty = y;
								pass = true;							
							}
						}
						break;
					case 2: 
						if(y!=0)
						{							
							if((y-1)!=prevy)
							{
								nextx = x;
								nexty = y-1;
								pass = true;
							}
						}
						break;
					case 3: 
						if(y!=19)
						{							
							if((y+1)!=prevy)
							{
								nextx = x;
								nexty = y+1;
								pass = true;
							}
						}
						break;					
					}//end switch
				}
			}//end while
		}
		else if((rooms[x][y].perceive.breeze)||(rooms[x][y].perceive.stench))
		{	
			//������ �� ���
			if(rooms[x][y].perceive.stench)
			{
				//ȭ���� ������ wumpus�� �ִٰ� �����ϴ� �� �߿� �� ���� ȭ��� ��
				//ȭ���� ������ �ٽ� ���ư�
				if(arrow)
				{
					boolean pass = false;	
					boolean kill = false;
					int newx = prevx, newy = prevy;
					while(!pass)
					{
						int sel = (int)(Math.random()*4);				
						switch(sel)
						{
						case 0:	
							if(x!=0)
							{												
								if(!rooms[x-1][y].visit)
								{
									arrow = false;
									if(rooms[x-1][y].wumpus)
									{
										rooms[x-1][y].wumpus = false;
										rooms[x-1][y].perceive.scream = true;
										//ai.infer[x-1][y].ok = true;
										kill = true;										
										for(int i=0;i<20;i++) for(int j=0;j<20;j++)	
											rooms[i][j].perceive.stench = false;
										newx = x-1; newy = y;
									}
								}
							}				
							break;
						case 1: 
							if(x!=19)
							{		
								if(!rooms[x+1][y].visit)
								{
									arrow = false;
									if(rooms[x+1][y].wumpus)
									{
										rooms[x+1][y].wumpus = false;
										rooms[x+1][y].perceive.scream = true;
										//ai.infer[x+1][y].ok = true;
										kill = true;										
										for(int i=0;i<20;i++) for(int j=0;j<20;j++)	
											rooms[i][j].perceive.stench = false;
										newx = x+1; newy = y;
									}												
								}
							}
							break;
						case 2: 
							if(y!=0)
							{					
								if(!rooms[x][y-1].visit)
								{
									arrow = false;
									if(rooms[x][y-1].wumpus)
									{
										rooms[x][y-1].wumpus = false;
										rooms[x][y-1].perceive.scream = true;
										//ai.infer[x][y-1].ok = true;
										kill = true;										
										for(int i=0;i<20;i++) for(int j=0;j<20;j++)	
											rooms[i][j].perceive.stench = false;
										newx = x; newy = y-1;
									}							
								}
							}
							break;
						case 3: 
							if(y!=19)
							{					
								if(!rooms[x][y+1].visit)
								{
									arrow = false;
									if(rooms[x][y+1].wumpus)
									{
										rooms[x][y+1].wumpus = false;
										rooms[x][y+1].perceive.scream = true;
										//ai.infer[x][y+1].ok = true;
										kill = true;										
										for(int i=0;i<20;i++) for(int j=0;j<20;j++)	
											rooms[i][j].perceive.stench = false;
										newx = x; newy = y+1;
									}						
								}
							}
							break;					
						}//end switch
					}//end while
					if(kill)
					{
						nextx = newx;
						nexty = newy;
						System.out.print("The Agent Killed the Wumpus at (");
						System.out.print(x);
						System.out.print(", ");
						System.out.print(y);
						System.out.println(")");
						return;
					}
					else
					{
						nextx = prevx;
						nexty = prevy;
						return;
					}
				}
				else
				{
					nextx = prevx;
					nexty = prevy;
					return;
				}				
			}
			if(rooms[x][y].perceive.breeze)
			{
				if(pathindex>1000)
				{
					boolean pass = false;			
					while(!pass)
					{
						int sel = (int)(Math.random()*4);				
						switch(sel)
						{
						case 0:	
							if(x!=0)
							{												
								if(!rooms[x-1][y].visit)
								{
									nextx = x-1;
									nexty = y;
									pass = true;							
								}
							}				
							break;
						case 1: 
							if(x!=19)
							{		
								if(!rooms[x+1][y].visit)
								{
									nextx = x+1;
									nexty = y;
									pass = true;							
								}
							}
							break;
						case 2: 
							if(y!=0)
							{					
								if(!rooms[x][y-1].visit)
								{
									nextx = x;
									nexty = y-1;
									pass = true;
								}
							}
							break;
						case 3: 
							if(y!=19)
							{					
								if(!rooms[x][y+1].visit)
								{
									nextx = x;
									nexty = y+1;
									pass = true;
								}
							}
							break;					
						}//end switch	
					}//end while
				}
				else
				{
					if(x==0 && y==0)
					{
						if(rooms[1][0].visit || rooms[0][1].visit)
						{
							if(rooms[1][0].visit)
							{
								nextx = 1;
								nexty = 0;
							}
							if(rooms[0][1].visit)
							{
								nextx = 0;
								nexty = 1;
							}
						}
						else
						{
							int sel = (int)(Math.random()*2);				
							switch(sel)
							{							
							case 0: 
								nextx = x+1;
								nexty = y;							
								break;							
							case 1: 
								nextx = x;
								nexty = y+1;					
								break;					
							}//end switch
						}						
					}
					else
					{
						nextx = prevx;
						nexty = prevy;
					}					
				}				
			}			
		}
	}//end of findnext
	
	public static void locatepits()
	{
		int m =1, n = 1;
		for(int i=0; i<80; i++)
		{
			m = 1; n = 1;			
			while(m==1 && n==1)
			{
				m = (int)(Math.random()*20+1);
				n = (int)(Math.random()*20+1);
				if((m!=1 || n!=1) && (rooms[m-1][n-1].gold==false) && (rooms[m-1][n-1].wumpus==false)
						&& (rooms[m-1][n-1].pit==false))
				{	
					rooms[m-1][n-1].pit = true;
					break;
				}
				else
				{	
					m = 1; n = 1;
				}								
			}			
		}
	}//end of locatepits
	
	public static void deletepits()
	{
		for(int i=0; i<20; i++)
		{
			for(int j=0; j<20; j++)
			{
				rooms[i][j].pit = false;
			}
		}
	}//end of deletepits
	
	public static void setstench(int x, int y)
	{
		if((x-1)!=0)
		{
			rooms[x-2][y-1].perceive.stench = true;
		}
		if((x+1)!=21)
		{
			rooms[x][y-1].perceive.stench = true;
		}
		if((y-1)!=0)
		{
			rooms[x-1][y-2].perceive.stench = true;
		}
		if((y+1)!=21)
		{
			rooms[x-1][y].perceive.stench = true;
		}
	}//end of setstench
	
	public static void setbreeze()
	{
		for(int x=1; x<21; x++)
		{
			for(int y=1; y<21; y++)
			{
				if(rooms[x-1][y-1].pit)
				{
					if((x-1)!=0)
					{
						rooms[x-2][y-1].perceive.breeze = true;
					}
					if((x+1)!=21)
					{
						rooms[x][y-1].perceive.breeze = true;
					}
					if((y-1)!=0)
					{
						rooms[x-1][y-2].perceive.breeze = true;
					}
					if((y+1)!=21)
					{
						rooms[x-1][y].perceive.breeze = true;
					}
				}
			}
		}
	}//end of setbreeze
		
	public static void outputstate()
	{
		PrintWriter output = null;
		try
		{
			output = new PrintWriter(new FileOutputStream("state.txt"));
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Error");
			System.exit(0);
		}		
		//output.println("  =========================================");
		int pitnum = 0;
		for(int i=19;i>=0;i--)
		{
			if(i>8)
			{
				output.print(i+1);
			}
			else
			{
				output.print(" ");
				output.print(i+1);
			}
			output.print("|");
			for(int j=0;j<20;j++)
			{
				//output.println("-----------------------------------------");				
				if(rooms[j][i].gold || rooms[j][i].wumpus )
				{
					if(rooms[j][i].gold)
						output.print("G|");
					if(rooms[j][i].wumpus)
						output.print("W|");
				}
				if(rooms[j][i].pit)
				{
					output.print("P|");
					pitnum++;
				}
				if(rooms[j][i].gold==false && rooms[j][i].wumpus==false && rooms[j][i].pit==false)
				{
					output.print(" |");
				}
			}
			output.println();
			//output.println("  =========================================");
		}
		output.println("   1 2 3 4 5 6 7 8 9 1011121314151617181920");
		output.close();
		System.out.println("Number of Pits : "+pitnum);
	}//end of outputstate

	public static void outputpercept()
	{
		PrintWriter output = null;
		try
		{
			output = new PrintWriter(new FileOutputStream("percept.txt"));
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Error");
			System.exit(0);
		}					
		for(int i=19;i>=0;i--)
		{
			if(i>8)
			{
				output.print(i+1);
			}
			else
			{
				output.print(" ");
				output.print(i+1);
			}
			output.print("|");
			for(int j=0;j<20;j++)
			{	
				if(rooms[j][i].perceive.glitter)
				{
					output.print("G|");
				}
				if(rooms[j][i].perceive.stench)
				{
					output.print("S|");
				}
				if((!rooms[j][i].perceive.glitter) && (!rooms[j][i].perceive.stench))
				{
					output.print(" |");
				}
			}
			if(i>8)
			{
				output.print(" ");
				output.print(i+1);
			}
			else
			{
				output.print("  ");
				output.print(i+1);
			}
			output.print("|");
			for(int j=0;j<20;j++)
			{							
				if(rooms[j][i].perceive.breeze)
				{
					output.print("B|");
				}
				else
				{
					output.print(" |");
				}
			}			
			output.println();			
		}
		output.print("   1 2 3 4 5 6 7 8 9 1011121314151617181920");
		output.println("    1 2 3 4 5 6 7 8 9 1011121314151617181920");
		output.close();		
	}//end of outputpercept
	
	public static void outputpath(int result, int cause)
	{
		PrintWriter output = null;
		try
		{
			output = new PrintWriter(new FileOutputStream("path.txt"));
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Error");
			System.exit(0);
		}	
		//����
		output.println("*** Path of Agent ***");		
		//int pathsize = pathx.size();		
		System.out.println("Path Length : "+pathindex);
		for(int i=0; i<pathindex; i++)
		{
			//int x = pathx.get(i);
			//int y = pathy.get(i);
			int x = pathx[i];
			int y = pathy[i];
			output.print("(");
			output.print(x+1);
			output.print(", ");
			output.print(y+1);
			output.println(")");
		}
		if(result == 1)
		{
			output.println("SUCCESS : The Agent Grabbed the GOLD!!!!!");
		}
		else
		{
			if(cause == 1)
				output.println("FAIL : The Agent Died because of Pit");
			else
				output.println("FAIL : The Agent Died because of Wumpus");
		}
		output.close();
	}//end of outputpath	
}//end of class
