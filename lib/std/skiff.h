#pragma once

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <setjmp.h>
#include <signal.h>

#define GC_DEBUG 0
#define TRY_CATCH 1

#include "skiff_string.h"
#include "skiff_list.h"
#include "skiff_exception.h"
#include "skiff_anyref.h"
#include "skiff_int.h"
#include "skiff_float.h"
#include "try_catch.h"

void skiff_print(skiff_string_t *);
void skiff_println(skiff_string_t *);

int main(int, char **);
int32_t skiff_main(skiff_list_t *);

void skiff_catch_0(skiff_catch_layer_t * layer, skiff_exception_t * ex)
{
    printf("Top Level Error Caught! Message: ");
    skiff_println(ex->message);
    longjmp(layer->current_catch_state, 1);
}

static void sig_handler(int sig, siginfo_t *dont_care, void *dont_care_either)
{
    skiff_exception_t * ex = (skiff_exception_t *) skalloc(1, sizeof(skiff_exception_t));
    ex->class_ptr = &skiff_exception_interface;
    if(sig == SIGSEGV) 
    {
        skiff_exception_new(ex, skiff_string_new(0, "Invalid access to storage!"));
    }
    else if(sig == SIGFPE)
    {
        skiff_exception_new(ex, skiff_string_new(0, "Erroneous arithmetic operation!"));
    }
    else if(sig == SIGABRT)
    {
        skiff_exception_new(ex, skiff_string_new(0, "Abnormal termination!"));
    }
    else
    {
        skiff_exception_new(ex, skiff_string_new(0, "Error code unknown!"));
        printf("signal %i\n", sig);
    }
    skiff_throw(ex);
}

int main(int argc, char * argv[])
{
    skiff_allocator_init();

    catch_layer_tail = catch_layer_head = calloc(1, sizeof(skiff_catch_layer_t));
    catch_layer_head->catch_class_ptr = &skiff_exception_interface;
    catch_layer_head->current_catch = skiff_catch_0;
    catch_layer_head->sp_ref_val = sp_ref;
    catch_layer_head->prev = catch_layer_head->next = NULL;

    struct sigaction sa;
    memset(&sa, 0, sizeof(struct sigaction));
    sigemptyset(&sa.sa_mask);
    sa.sa_flags     = SA_NODEFER;
    sa.sa_sigaction = sig_handler;
    if(TRY_CATCH)
    {
        sigaction(SIGSEGV, &sa, NULL);
        sigaction(SIGFPE, &sa, NULL);
        sigaction(SIGABRT, &sa, NULL);
    }

    int32_t rtn = 1;
    int skiff_continue_exec_0 = setjmp(catch_layer_tail->current_catch_state);
    if(skiff_continue_exec_0 == 0)
    {
        skiff_list_t ** argz = skalloc_ref_stack();
        *argz = skiff_list_new(0, argc);
        for(size_t i = 0; i < argc; i++)
        {
            skiff_list_assign_sub_int(*argz, skiff_string_new(0, argv[i]), i);
        }
        rtn = skiff_main(*argz);
    }
    skfree_ref_stack(1);
    // run_gc();
    if(eden_ref != 0 && GC_DEBUG)
    {
        printf("Eden space not emptied!\n");
    }
    if(survivor_ref != 0 && GC_DEBUG)
    {
        printf("Survivor space not emptied!\n");
    }
    return rtn;
}

// skiff_string_t * skiff_int_to_string(int32_t this) 
// {
//     char snum[12];
//     itoa(this, snum, 10);
//     return skiff_string_allocate_new(snum);
// }

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

bool _instance_of(struct skiff_any_ref_class_struct * type1, 
                    struct skiff_any_ref_class_struct * type2)
{
    if(type1 == type2)
    {
        return true;
    }
    if(type1->parent == NULL)
    {
        return false;
    }
    return _instance_of(type1->parent, type2);
}

bool instance_of(skiff_any_ref_t * obj, void * type) 
{
    return _instance_of(obj->class_ptr, type);
}