
public class Agent {	
	public Infer[][] infer;	//agent�� �߷��� ���
	public Agent()
	{
		 infer = new Infer[20][20];
		 for(int i=0;i<20;i++)
		{
			for(int j=0;j<20;j++)
			{
				infer[i][j] = new Infer();
			}
		}
	}
}
