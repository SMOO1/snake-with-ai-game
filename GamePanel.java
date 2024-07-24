import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import java.awt.image.*;

import java.io.*;
import java.io.IOException;

public class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 35;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static final int DELAY = 100;
    
    final int X[] = new int[GAME_UNITS];
    final int Y[] = new int[GAME_UNITS];
    final int[] aiX = new int[GAME_UNITS];
    final int[] aiY = new int[GAME_UNITS];
    
    static final String HIGH_SCORE_FILE = "highscore.txt";
    int highScore;
    
    int bodyParts = 2;
    int applesEaten;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;
    List<Point> borders;
    
    BufferedImage appleImage; 

    GamePanel() {
        random = new Random();
        borders = new ArrayList<>();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
        
        try {
            appleImage = ImageIO.read(new File("apple.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        X[0] = 0; // Initial position for player snake head
        Y[0] = 0;
        aiX[0] = 0; // Initial position for AI snake head
        aiY[0] = 0;
        
        loadHighScore(); 
    }

    public void moveAI() {
        int newX = aiX[0];
        int newY = aiY[0];
        
        // Randomly choose a direction
        int direction = random.nextInt(4);

        switch (direction) {
            case 0: // UP
                newY -= UNIT_SIZE;
                break;
            case 1: // DOWN
                newY += UNIT_SIZE;
                break;
            case 2: // LEFT
                newX -= UNIT_SIZE;
                break;
            case 3: // RIGHT
                newX += UNIT_SIZE;
                break;
        }

        // Check if the new position is within the borders and doesn't collide with itself
        if (newX >= 0 && newX < SCREEN_WIDTH && newY >= 0 && newY < SCREEN_HEIGHT) {
            boolean collision = false;
            for (int i = bodyParts; i > 0; i--) {
                if (newX == aiX[i] && newY == aiY[i]) {
                    collision = true;
                    break;
                    }
                }
            
            // Check if AI snake collides with the borders
            for (Point point : borders) {
                if (newX == point.x && newY == point.y) {
                    collision = true;
                    break;
                }
            }
            if (!collision) {
                // Update AI snake position
                for (int i = bodyParts - 1; i > 0; i--) {
                    aiX[i] = aiX[i - 1];
                    aiY[i] = aiY[i - 1];
                }
                aiX[0] = newX;
                aiY[0] = newY;
    
                // Check if AI snake collides with the apple
                if (aiX[0] == appleX && aiY[0] == appleY) {
                    bodyParts++;
                    applesEaten++;
                    newApple();
                    createBorder();
                }
            }
        }
    }

    public void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
    
    public void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateHighScore() {
        if (applesEaten > highScore) {
            highScore = applesEaten;
            saveHighScore();
        }
    }


    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            // draw the grid lines on the screen
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            }

            // draw the food on the screen at a random location
            g.drawImage(appleImage, appleX, appleY, UNIT_SIZE, UNIT_SIZE, this);

            // draw the body of the snake
            for (int i = 1; i < bodyParts; i++) {
                g.setColor(new Color(45, 180, 0)); // green color for the body
                g.fillRect(X[i], Y[i], UNIT_SIZE, UNIT_SIZE);
            }

            // draw the head of the snake
            g.setColor(Color.green); // green color for the head
            g.fillRect(X[0], Y[0], UNIT_SIZE, UNIT_SIZE);

            // draw borders
            g.setColor(Color.gray);
            for (Point point : borders) {
                g.fillRect(point.x, point.y, UNIT_SIZE, UNIT_SIZE);
            }

            // Draw AI snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.red); // for the AI head
                } else {
                    g.setColor(Color.orange); // for the AI body
                }
                g.fillRect(aiX[i], aiY[i], UNIT_SIZE, UNIT_SIZE);
            }


            // draw the score
            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            X[i] = X[i - 1];
            Y[i] = Y[i - 1];
        }
        switch (direction) {
            case 'U':
                Y[0] = Y[0] - UNIT_SIZE;
                break;
            case 'D':
                Y[0] = Y[0] + UNIT_SIZE;
                break;
            case 'L':
                X[0] = X[0] - UNIT_SIZE;
                break;
            case 'R':
                X[0] = X[0] + UNIT_SIZE;
                break;
        }
    }

    public void checkApple() {
        if ((X[0] == appleX) && (Y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
            createBorder();
        }
    }

    public void checkCollision() {
        // Check if player snake collides with its own body
        for (int i = bodyParts; i > 0; i--) {
            if ((X[0] == X[i]) && (Y[0] == Y[i])) {
                running = false;
                break;
            }
        }

        // Check if player snake collides with the AI snake's body (including head)
        for (int i = 0; i < bodyParts; i++) {
            if ((X[0] == aiX[i]) && (Y[0] == aiY[i])) {
                running = false;
                break;
            }
        }

        // Check if player snake collides with the borders
        if (X[0] < 0 || X[0] >= SCREEN_WIDTH || Y[0] < 0 || Y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        // Check if player snake collides with the borders
        for (Point point : borders) {
            if (X[0] == point.x && Y[0] == point.y) {
                running = false;
                break;
            }
        }

        // Check if AI snake collides with the borders
        for (Point point : borders) {
            if (aiX[0] == point.x && aiY[0] == point.y) {
                running = false;
                break;
            }
        }

        if (!running) {
            timer.stop();
        }
    }

    public void gameOver(Graphics g) {
        // score
        updateHighScore(); 
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
        // Game over text
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);
        
        // Display high score at top right
        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 20));
        g.drawString("High Score: " + highScore, SCREEN_WIDTH - 150, 30);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollision();
            moveAI(); 
        }
        repaint();
    }

    public void createBorder() {
        int borderX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        int borderY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
        borders.add(new Point(borderX, borderY));
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
            }
        }
    }
}
