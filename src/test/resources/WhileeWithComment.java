class WhileeWithComment
{
    /* SomeComment1 */
    public static void main(String[] args)
    {
        int i = 0;
        /* SomeComment2 */
        while(i < 3)
        {
            /* SomeComment3 */
            i = /* SomeComment4 */i + 1;
        }
        System.out.print(i);
    }
}