package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;


public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }


    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }


    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validOnes = new ArrayList<>();
        for (ChessMove move : moves) {
            if (!willBeInCheck(piece.getTeamColor(), move)) {
                validOnes.add(move);
            }
        }
        return validOnes;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        if (board.getPieces()[8 - start.getRow()][start.getColumn() - 1] != null &&
                validMoves(start).contains(move) && board.getPiece(start).getTeamColor() == teamTurn) {
            if (move.getPromotionPiece() == null) {
                board.addPiece(move.getEndPosition(), board.getPiece(start));
                board.addPiece(start, null);
            } else {
                board.addPiece(move.getEndPosition(), new ChessPiece(this.teamTurn, move.getPromotionPiece()));
                board.addPiece(start, null);
            }
            if (teamTurn == TeamColor.WHITE) {
                setTeamTurn(TeamColor.BLACK);
            } else {
                setTeamTurn(TeamColor.WHITE);
            }
        } else {
            throw new InvalidMoveException();
        }
    }

    private Collection<ChessMove> getPossibleMoves(TeamColor teamColor) {
        Collection<ChessMove> moves = new ArrayList<>();
        for (int i = 1; i < board.getPieces().length + 1; i++) {
            for (int j = 1; j < board.getPieces().length + 1; j++) {
                if (board.getPieces()[8 - i][j - 1] != null) {
                    ChessPosition position = new ChessPosition(i, j);
                    if (board.getPiece(position).getTeamColor() == teamColor) {
                        moves.addAll(board.getPiece(position).pieceMoves(board, position));
                    }
                }
            }
        }
        return moves;
    }


    public boolean isInCheck(TeamColor teamColor) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        if(teamColor == TeamColor.WHITE){possibleMoves = getPossibleMoves(TeamColor.BLACK);}
        else{possibleMoves = getPossibleMoves(TeamColor.WHITE);}
        for (ChessMove move : possibleMoves) {
            ChessPiece piece = board.getPiece(move.getEndPosition());
            if (board.getPiece(move.getEndPosition()) != null && piece.getPieceType() == ChessPiece.PieceType.KING) {
                if(piece.getTeamColor() == teamColor){return true;}
            }
        }
        return false;
    }


    /** Used by isInStalemate and isInCheckMate to see if a piece will be in check if a certain move is made.*/
    private boolean willBeInCheck(TeamColor teamColor, ChessMove move) {
        ChessPiece temp = board.getPiece(move.getEndPosition());
        board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
        board.addPiece(move.getStartPosition(), null);

        if (isInCheck(teamColor)) {
            ChessMove backMove = new ChessMove(move.getEndPosition(), move.getStartPosition(), move.getPromotionPiece());
            board.addPiece(move.getStartPosition(), board.getPiece(move.getEndPosition()));
            board.addPiece(backMove.getStartPosition(), temp);
            return true;
        }

        ChessMove backMove = new ChessMove(move.getEndPosition(), move.getStartPosition(), move.getPromotionPiece());
        board.addPiece(backMove.getEndPosition(), board.getPiece(backMove.getStartPosition()));
        board.addPiece(backMove.getStartPosition(), temp);
        return false;
    }


    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        boolean isStalemate = false;
        Collection<ChessMove> moves = getPossibleMoves(teamColor);
        for (ChessMove move : moves) {
            if (!willBeInCheck(teamColor, move)) {
                return false;
            } else {
                isStalemate = true;
            }
        }
        return isStalemate;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        boolean isCheckmate = false;
        Collection<ChessMove> moves = getPossibleMoves(teamColor);
        if (isInCheck(teamColor)) {
            for (ChessMove move : moves) {
                if (!willBeInCheck(teamColor, move)) {
                    return false;
                }
            }
            isCheckmate = true;
        }
        return isCheckmate;
    }


    public void setBoard(ChessBoard board) {
        this.board = board;
    }


    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "teamTurn=" + teamTurn +
                ", board=" + board +
                '}';
    }
}
