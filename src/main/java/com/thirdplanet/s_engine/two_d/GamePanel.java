package com.thirdplanet.s_engine.two_d;//http://fivedots.coe.psu.ac.th/~ad/jg/ch1/index.html
//http://fivedots.coe.psu.ac.th/~ad/jg/ch1/ch1.pdf
import javax.swing.* ;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel implements Runnable {
        private static final int PWIDTH = 500 ;
        private static final int PHEIGHT = 400 ;

        private Thread animator = null  ;
        private boolean running = false ;
        private boolean gameOver = false ;

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
        public void addNotify(){
                super.addNotify() ;
                startGame() ;
        }
        private void startGame(){
                if(animator != null || running != false){
                        animator = new Thread(this) ;
                        animator.start() ;
                }
        }
        public void stopGame(){
                running = false ;
        }
        private void testPress(int x, int y){
                //do something
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
        public void gameRender(){
                if(dbImage == null){
                        System.out.println("dbImage is null") ;
                        return ;
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
        public void run(){
                running =  true ;
                while(running){
                        gameUpdate() ;
                        gameRender();
                        paintScreen();
                        try{
                                Thread.sleep(20);
                        }catch(InterruptedException ex){}
                }
                System.exit(0);
        }
        private void gameUpdate(){
                //....
        }
}
