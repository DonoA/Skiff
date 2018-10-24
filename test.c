#include <stdio.h>
#include <stdio.h>
int main(int argc, char **argv);
signed int helloWorld_1 (unsigned char * say);
int main(int argc, char **argv)
{
unsigned char * a = "Hello World";
signed int (*helloWorld)(unsigned char *) = &helloWorld_1;
signed int b = (*helloWorld)(a);
printf("%i\n",b + 10);
}
signed int helloWorld_1 (unsigned char * say)
{
printf("%s\n",say);
return 35;
}
