package com.example.chessapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Stack;

public class ChessGameActivity extends AppCompatActivity {

    private ImageButton[][] chessBoard;
    private ImageButton selectedCellFrom;
    private ImageButton selectedCellTo;
    private Stack<Move> moveStack;
    private Stack<Move> redoStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_game);

        moveStack = new Stack<>();
        redoStack = new Stack<>();
        chessBoard = new ImageButton[8][8];

        GridLayout chessboardLayout = findViewById(R.id.chessboard);
        initializeChessboard(chessboardLayout);

        Button makeMoveButton = findViewById(R.id.makeMoveButton);
        Button undoButton = findViewById(R.id.undoButton);
        Button redoButton = findViewById(R.id.redoButton);

        makeMoveButton.setOnClickListener(v -> {
            if (selectedCellFrom != null && selectedCellTo != null) {
                makeMove(selectedCellFrom, selectedCellTo);
            } else {
                Toast.makeText(this, "Select a piece and a target cell first", Toast.LENGTH_SHORT).show();
            }
        });

        undoButton.setOnClickListener(v -> undoMove());
        redoButton.setOnClickListener(v -> redoMove());
    }

    private void initializeChessboard(GridLayout chessboardLayout) {
        int cellSize = getResources().getDisplayMetrics().widthPixels / 8;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ImageButton cell = new ImageButton(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                cell.setLayoutParams(params);

                cell.setScaleType(ImageButton.ScaleType.CENTER_CROP);
                cell.setTag(row + "," + col);
                cell.setOnClickListener(this::onCellClick);
                chessBoard[row][col] = cell;

                // Set chess pieces in initial positions
                if (row == 1) cell.setImageResource(R.drawable.black_pawn);
                else if (row == 6) cell.setImageResource(R.drawable.white_pawn);
                else if (row == 0 || row == 7) {
                    if (col == 0 || col == 7) cell.setImageResource(row == 0 ? R.drawable.black_rook : R.drawable.white_rook);
                    else if (col == 1 || col == 6) cell.setImageResource(row == 0 ? R.drawable.black_knight : R.drawable.white_knight);
                    else if (col == 2 || col == 5) cell.setImageResource(row == 0 ? R.drawable.black_bishop : R.drawable.white_bishop);
                    else if (col == 3) cell.setImageResource(row == 0 ? R.drawable.black_queen : R.drawable.white_queen);
                    else if (col == 4) cell.setImageResource(row == 0 ? R.drawable.black_king : R.drawable.white_king);
                }

                // Alternate colors for a classic chessboard look
                if ((row + col) % 2 == 0) {
                    cell.setBackgroundColor(0xFFCCCCCC); // Light color
                } else {
                    cell.setBackgroundColor(0xFF333333); // Dark color
                }
                chessboardLayout.addView(cell);
            }
        }
    }

    private void onCellClick(View view) {
        ImageButton clickedCell = (ImageButton) view;

        if (selectedCellFrom == null) {
            selectedCellFrom = clickedCell;
            clickedCell.setBackgroundColor(0xFFFFD700);  // Highlight selected source cell
        } else if (selectedCellTo == null && clickedCell != selectedCellFrom) {
            selectedCellTo = clickedCell;
            clickedCell.setBackgroundColor(0xFFFFD700);  // Highlight selected target cell
        } else {
            resetCellSelections();
        }
    }

    private void makeMove(ImageButton fromCell, ImageButton toCell) {
        if (fromCell.getDrawable() == null) {
            Toast.makeText(this, "No piece selected to move", Toast.LENGTH_SHORT).show();
            return;
        }

        String fromPosition = (String) fromCell.getTag();
        String toPosition = (String) toCell.getTag();

        // Create a new move and push it to the move stack
        Move newMove = new Move(fromPosition, toPosition);
        moveStack.push(newMove);
        redoStack.clear(); // Clear redo stack after a new move

        // Move the piece image to the target cell
        toCell.setImageDrawable(fromCell.getDrawable());
        fromCell.setImageDrawable(null); // Clear the source cell

        // Display a toast with the move details
        Toast.makeText(this, "Moved from " + fromPosition + " to " + toPosition, Toast.LENGTH_SHORT).show();

        // Reset cell selections for the next move
        resetCellSelections();
    }

    private void resetCellSelections() {
        if (selectedCellFrom != null) {
            selectedCellFrom.setBackgroundColor((getCellPosition(selectedCellFrom) % 2 == 0) ? 0xFFCCCCCC : 0xFF333333);
        }
        if (selectedCellTo != null) {
            selectedCellTo.setBackgroundColor((getCellPosition(selectedCellTo) % 2 == 0) ? 0xFFCCCCCC : 0xFF333333);
        }
        selectedCellFrom = null;
        selectedCellTo = null;
    }

    private void undoMove() {
        if (!moveStack.isEmpty()) {
            Move lastMove = moveStack.pop();
            redoStack.push(lastMove);
            String fromPosition = lastMove.getFrom();
            String toPosition = lastMove.getTo();

            ImageButton fromCell = getCellByPosition(fromPosition);
            ImageButton toCell = getCellByPosition(toPosition);

            fromCell.setImageDrawable(toCell.getDrawable());
            toCell.setImageDrawable(null);
        } else {
            Toast.makeText(this, "No moves to undo", Toast.LENGTH_SHORT).show();
        }
    }

    private void redoMove() {
        if (!redoStack.isEmpty()) {
            Move redoMove = redoStack.pop();
            moveStack.push(redoMove);
            String fromPosition = redoMove.getFrom();
            String toPosition = redoMove.getTo();

            ImageButton fromCell = getCellByPosition(fromPosition);
            ImageButton toCell = getCellByPosition(toPosition);

            toCell.setImageDrawable(fromCell.getDrawable());
            fromCell.setImageDrawable(null);
        } else {
            Toast.makeText(this, "No moves to redo", Toast.LENGTH_SHORT).show();
        }
    }

    private int getCellPosition(View cell) {
        String tag = (String) cell.getTag();
        String[] position = tag.split(",");
        return Integer.parseInt(position[0]) * 8 + Integer.parseInt(position[1]);
    }

    private ImageButton getCellByPosition(String position) {
        String[] coords = position.split(",");
        int row = Integer.parseInt(coords[0]);
        int col = Integer.parseInt(coords[1]);
        return chessBoard[row][col];
    }
}
