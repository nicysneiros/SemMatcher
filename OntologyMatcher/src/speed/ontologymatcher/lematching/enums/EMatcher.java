package speed.ontologymatcher.lematching.enums;

public enum EMatcher {

	HMATCH(1),
	ALIGNMENT_API(2),
	HMATCHCL(3),
	NONE(4)
	;
	
	private int matcher = 4;
	
	private EMatcher(int num)
	{
		this.matcher = num;		
	}
	
	public static EMatcher create(int num)
	{
		EMatcher matcher = EMatcher.NONE;
		
		switch(num)
		{
			case 1:
				matcher = EMatcher.HMATCH;
				break;
			case 2:
				matcher = EMatcher.ALIGNMENT_API;
				break;
			case 3:
				matcher = EMatcher.HMATCHCL;
				break;		
		}
		
		return matcher;
	}
	
	@Override	
	public String toString() {
		return this.matcher + "";
	}
	
}
