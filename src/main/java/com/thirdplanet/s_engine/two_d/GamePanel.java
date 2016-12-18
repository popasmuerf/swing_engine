http://fivedots.coe.psu.ac.th/~ad/jg/ch1/index.html
import javax.swing.* ;

public class GamePanel extends JPanel implements Runnable{
        private static final int PWIDTH = 500 ;
        private static final int PHEIGHT = 400 ;

        private Thread animator ;
        private Thread boolean running = false ;
        private boolean gameOver = false ;

        private Graphics dbg ;
        private Image dbImage = null ;

        public GamePanel(){
                setBackground(Color.white) ;
                setPreferredSize(new Dimension(PWIDTH,PHEIGHT) ;
        }
        public void addNotify(){
                super.addNotify() ;
                startGame() ;
        }
        private void startGame(){
                if(!animator || !running){
                        animator = new Thread(this) ;
                        animator.start() ;
                }
        }
        public void stopGame(){
                running = false ;
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
        public void run(){
                running =  true ;
                while(running){
                        gameUpdate() ;
                        gameRender();
                        repaint();
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
