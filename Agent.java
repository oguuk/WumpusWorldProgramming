
public class Agent {	
	public Infer[][] infer;	//agent가 추론한 결과
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
