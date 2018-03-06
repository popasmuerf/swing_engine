package com.thirdplanet.s_engine.two_d;//http://fivedots.coe.psu.ac.th/~ad/jg/ch1/index.html
//http://fivedots.coe.psu.ac.th/~ad/jg/ch1/ch1.pdf
import javax.swing.* ;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;

/**
 * A core technology for a good game is an animation
 * algorithm that produces reliably fast game play
 * across various operating systems(e.g. flavors of Widows, Linux an d
 * OSX)...and in different types of jvm based programs, applets
 * , windoewd , and full-scrren applications).
 *
 * GamePanel is a JPanel subclass witch acts as a 2D canvas
 * for drawing 2D graphics(lines, circles, text, images).
 *
 * Animation is managed by a thread, which ensures that it
 * it progresses at a fairly consistent rate(FPS), where a frame
 * corresponds to a sinle rendering of the application canvas.
 *
 * {update, render, sleep} animation loop
 *
 * -Starting and terminating an animation
 * -double buffering
 * -user interaction
 * -active rendering
 * -andimation control based on a user's requested FPS
 * -management of inaccuracies in the timer and sleep operations
 * -Combining FPS and game state updates per seconds(UPS)
 * -Game pausing and resumption
 *
 * Threads:  We have two main ones here
 * -----------------------------------------------------
 * 1. Main thread(GUI, events)
 * 2. animator thread(updates and rendering)
 * JMM deals with this via forcing atomicity on all
 * variables save for longs and doubles.
 * 3. Set variables whom state is globally important to volatile
 * so that their currently extant values may not be copied to local
 * thread memory.
 *
 *
 * Why are making the animation thread sleep for a few "moments ?
 * -----------------------------------------------------------------
 * We want to give the jvm time to garbage collect....or rather
 * ensure that the jvm will garbage collect....otherwise there is
 * a real possiblity that our animation thread will hog up most of the
 * CPU time(I am guessing...need more infor on how the jvm does things
 * these days..)
 *
 * We also want to give the preceding call to repaint() time to finish...
 * everytime we call repaint()  the request is placed on the jvm's
 * event queue and then returns.  When this request is completed is
 * not up to us....
 *
 * The repaint request will be processed, precolating down
 * through the components of the application until
 * GamePanel's paintComponent() is called....so is 20 ms to
 * generous ?  Maybe it's just right...as it also prevents event
 * coalescence...where a jvm overloaded with events may drop some...
 * bad juju.....
 */
public class GamePanel extends JPanel implements Runnable {
        private static final int PWIDTH = 500 ;
        private static final int PHEIGHT = 400 ;

        private Thread animator = null  ;
        private volatile boolean running = false ;
        private volatile boolean gameOver = false ;
        private volatile boolean isPaused = false ;

        private static int MAX_FRAME_SKIPS = 5 ;
        private static int NO_DELAYS_PER_YIELD = 0 ;
        private Graphics dbg ;
        private Image dbImage = null ;
        private String msg = "" ;
        private int x = 0 ;
        private int y = 0 ;

        public GamePanel() {
                setBackground(Color.white);
                setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
                setFocusable(true);
                requestFocus();//JPanel can now receive key events
                readyForTermination();
                this.addMouseListener(new MouseAdapter() {
                        public void mousePressed(MouseEvent e){
                                testPress(e.getX(), e.getY());
                        }
                });
        }
        /**Wait for the JPanel to be added to the
         * JFrame/JApplet before starting via
         * creating a peer and then starting the
         * thread.
         *
         * About addNotify():
         * Makes this component displayable by connecting
         * it to a native screen resource.
         * This method is called internlly by the toolkit
         * and usually should not be called diretly
         * by programs...
         *
         * */
        public void addNotify(){
                super.addNotify() ;
                startGame() ;
        }

        /**
         * Initialize and start the thread
         */
        private void startGame(){
                if(animator != null || running != false){
                        animator = new Thread(this) ;
                        animator.start() ;
                }
        }
        /**Kills game by setting running flag
         * to false */
        public void stopGame(){
                running = false ;
        }

        /**For testing mouse activity ....   */
        private void testPress(int x, int y){
                //is (x,y) is important to the game ?
                if(!isPaused && !gameOver){
                        //do something
                }

        }
        private void readyForTermination(){
                this.addKeyListener(new KeyAdapter(){
                        public void keyPressed(KeyEvent e){
                                int keyCode = e.getKeyCode();
                                if((keyCode == KeyEvent.VK_ESCAPE) ||(keyCode == KeyEvent.VK_Q)||(keyCode == KeyEvent.VK_END)||(keyCode == KeyEvent.VK_C)&& e.isControlDown()){
                                        running = false ;
                                }
                        }
                });
        }
        private void gameOverMessage(Graphics g){
                g.drawString(msg,x,y);
        }
        public void paintComponent(Graphics g){
                super.paintComponent(g);
                if(dbImage!=null){
                        g.drawImage(dbImage,0,0, null);
                }
        }

        /**
         * Even with the most exciting game, there comes a a time
         * when the user wants to pause it(and resume later).
         *
         * One largely discredited coding appraoch is to use
         * Thread.suspend() and resume.  these methods are deprecated
         * for a similar reason to Thread.stop(); suspend() can
         * cause applet/application to suspend at any point in its
         * execution.  Tis can easily lead to deadlock...as the thread
         * is holding a resource since it will be releasedunilt the
         * thread resumes.
         *
         * Instead, use Thread.wait() ; and Thread.notify to implement
         * puase and resume functionality.  The idea here is to suspend the
         * animation thread, but the event dispatcher thread will still
         * respond to GUI activity.
         *
         * Though the elements of the game seen by the user
         * can pause, it is often useful for the other parts
         * to contine executiong.  For example, in a network
         * game, it may be necessary to monitor sockets for
         * messages comming from other players....
         *
         * Key presses are still handled by  the KeyListener method
         * since it must be possible to quit even in the paused state....
         *
         *
         * We don't want isPaused() to be monitored in the run() method
         * since the animation thread doesn't suspend.  isPaused() is
         * used to switch off testPress() and gameUpdate()
         *
         *
         */

        public void pauseGame(){
                isPaused = true ;
        }
        public void resumeGame(){
                isPaused = false ;
        }

        /**gameRender() draws into its own Graphics object(dbg), which
         * represents an image the same size as the screen(dbImage)
         * It draws the into its own Graphics object(dbj), which represents
         * an image the same size as teh screen (dbImage)*/
        public void gameRender(){
                if(dbImage == null){
                        dbImage = createImage(PWIDTH,PHEIGHT);
                        if(dbImage == null){
                            System.out.println("dbImage is null") ;
                            return ;
                        }
                }else{
                        dbg = dbImage.getGraphics();
                        dbg.setColor(Color.white);
                        dbg.fillRect(0,0,PWIDTH,PHEIGHT);
                        //draw game elements
                        if(gameOver){
                                gameOverMessage(dbg) ;
                        }
                }
        }
    /**Previously we drew our image in gameRender()...this was
     * our first buffer...now
     * ...this is our 2nd buffer.....
     * we use drawImage() which is fast enough to make things seem
     * instantaneous...*/
        private void paintScreen(){
                Graphics g ;
                try{
                        g = this.getGraphics();
                        if((g!=null)&&(dbImage !=null)){
                                g.drawImage(dbImage,0,0,null);
                                g.dispose();
                        }
                }catch(Exception e){
                        System.out.println("Graphics context error: " + e);
                }
        }
        /**Repeatedly updates, renders, sleeps */
        public void run(){
                /*
                   Repeatedly update, render, sleep
                   so loop takes close  to period nsecs.

                   Sleep inaccuracies are handled.
                   The timing calculation used the Java
                   timer.

                   Overruns in update/renders will
                   cause extra updates to be
                   carried out so UPS tild==requested FPS

                 */
                long beforeTime ;
                long afterTime  ;
                //long beforeNanoTime ;
                //long beforeJ3DTime ;
                long overSleepTime = 0L ;
                int noDelays = 0 ;
                long excess = 0L ;
                long timeDiff ;
                long sleepTime ;
                long period = 10 ;
                beforeTime = System.nanoTime();
                running =  true ;

                while(running) {
                        gameUpdate(); //gameUpdate-1
                        //gameUpdate(); //gameUpdate-2
                        gameRender();
                        paintScreen();
                        afterTime = System.nanoTime();
                        timeDiff = afterTime - beforeTime;
                        sleepTime = (period - timeDiff) - overSleepTime;

                        if (sleepTime > 0) {
                                try {
                                        Thread.sleep(sleepTime / 1000000L); //nano - >ms;
                                } catch (InterruptedException e) {
                                }
                                overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
                        } else {
                                //sleepTime <= 0 ; then frame took longer than the period
                                excess -= sleepTime;
                                overSleepTime = 0L;
                                if (++noDelays >= NO_DELAYS_PER_YIELD) {
                                        Thread.yield(); //let some other thread run
                                        noDelays = 0;
                                }
                        }
                        beforeTime = System.nanoTime();
                        /*
                                If frame animation is taking too long,
                                update the game state without  rendenring
                                it, to gt the updates/nearer to the
                                required FPS
                         */
                        int skips = 0;
                        while ((excess > period) && (skips < MAX_FRAME_SKIPS)) {
                                excess -= period;
                                gameUpdate(); //update state but don't render
                        }
                        System.exit(0);
                /*

                        if(timeDiff<= 0){
                                sleepTime = 5 ;
                        }
                        try{
                                Thread.sleep(20);
                        }catch(InterruptedException ex){}
                        beforeTime = System.nanoTime() ;
                }
                System.exit(0);
                */
                }
        }
        /**
         * We update the game state here....
         * */
        private void gameUpdate(){
                /**
                 * We implementing pausing
                 * the game not by stopping the
                 * game run() thread because
                 * that could cause emergent
                 * deadlocks....no...we should
                 * only prvent the game from updating
                 * while letting the game run thread to run...
                 * also...if no for other reasons such as network
                 * updates can still be processed...
                 *
                 *
                 * When should we pause/resume ?
                 * When the user returns to the page, the applet/program
                 * starts again....the same sequence should be triggered when
                 * the user minimizes the applet's page and reopens it later.....
                 *
                 * in any application, pausing should be initiated when the window
                 * is minimized or eactivated, and execution should resume when the
                 * window is enlarged or activated.  A window is deactivated when it is
                 * obsucred and activated when broung back to the front.
                 *
                 *
                 * In a a full-screen application pausing and resumpition will be
                 * controlled by buttons on the canvas since the user
                 * interface lacks a tigle bar and the OS taskbar is hidden.
                 *
                 *
                 */
                if(!isPaused && !gameOver){
                        //update game state
                }
        }

}
