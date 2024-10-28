extern "C" {

	#include <omp.h>
	#include <stdio.h>
	
	int mainb(void)
	{
		#pragma omp parallel
		{
			printf("mao");
		}

		return 0;
	}

}
