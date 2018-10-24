#include <stdio.h>
int main(int argc, char **argv);
void helloWorld_1 (unsigned char * say);
int main(int argc, char **argv)
{
unsigned char * a = "Hello World";
void (*helloWorld)(unsigned char *) = &helloWorld_1;
(*helloWorld)(a);
}
void helloWorld_1 (unsigned char * say)
{
printf("%s\n",say);
printf("%s %s\n",say,say);
return ;
}
