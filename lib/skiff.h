#pragma once

#include <stdint.h>
#include <stdio.h>
#include <setjmp.h>
#include "skiff_string.h"
#include "skiff_list.h"
#include "skiff_exception.h"
#include "try_catch.h"

void skiff_print(skiff_string_t *);
void skiff_println(skiff_string_t *);

int main(int, char **);
int32_t skiff_main(skiff_list_t *);

void skiff_catch_0(skiff_catch_layer_t * layer, skiff_exception_t * ex)
{
    printf("Error caught!\n");
    skiff_println(ex->message);
    longjmp(layer->current_catch_state, 0);
}

int main(int argc, char * argv[])
{
    catch_layer_tail = catch_layer_head = calloc(1, sizeof(skiff_catch_layer_t));
    catch_layer_head->catch_class_ptr = &skiff_exception_interface;
    catch_layer_head->current_catch = skiff_catch_0;
    catch_layer_head->sp_ref_val = sp_ref;
    catch_layer_head->prev = catch_layer_head->next = NULL;

    int skiff_continue_exec_0 = setjmp(catch_layer_tail->current_catch_state);
    if(skiff_continue_exec_0 == 0)
    {
        skiff_list_t * argz = skiff_list_allocate_new();
        for(size_t i = 0; i < argc; i++)
        {
            skiff_list_append(argz, skiff_string_allocate_new(argv[i]));
        }
        int32_t rtn = skiff_main(argz);
        return rtn;
    }

    return 1;
}

void skiff_println(skiff_string_t * string)
{
    skiff_print(string);
    putchar('\n');
}

void skiff_print(skiff_string_t * string)
{
    for(size_t i = 0; i < string->len; i++) 
    {
        putchar(string->data[i]);
    }
}