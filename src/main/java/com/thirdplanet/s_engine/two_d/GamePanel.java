public class GamePanel extends JPanel implements Runnable{
        private static final int PWIDTH = 500 ;
        private static final int PHEIGHT = 400 ;

        private Thread animator ;
        private Thread boolean running = false ;
        private boolean gameOver = false ;
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
