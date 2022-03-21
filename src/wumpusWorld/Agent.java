package wumpusWorld;

public class Agent {	
	public Infer[][] infer;	//agent가 추론한 결과
	public Agent()
	{
		 infer = new Infer[4][4];
		 for(int i=0;i<4;i++)
		{
			for(int j=0;j<4;j++)
			{
				infer[i][j] = new Infer();
			}
		}
	}
}
